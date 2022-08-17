/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.datastore;

import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.runtime.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.datastore.BuildTaskDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Stateless
public class DefaultBuildTaskDatastore implements BuildTaskDatastore {

    private static final Logger log = LoggerFactory.getLogger(DefaultBuildTaskDatastore.class);

    private final Set<BuildCoordinationStatus> UNFINISHED_OR_FAILED_STATES = EnumSet
            .complementOf(BuildCoordinationStatus.successfulFinishStates());

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void persist(BuildTask task) {
        log.info("persisting task with id {}", task.getId());
        entityManager.persist(task);
    }

    @Override
    public void remove(BuildTask task) {
        log.info("removing task " + task.getId());
        entityManager.createQuery("DELETE FROM BuildTask t where t.id = :taskId").setParameter("taskId", task.getId());
    }

    @Override
    public BuildTask getTask(String id) {
        return entityManager.createQuery("SELECT t FROM BuildTask t WHERE t.id = :taskId", BuildTask.class)
                .setParameter("taskId", id)
                .getSingleResult();
    }

    @Override
    public Optional<BuildTask> getTask(
            BuildConfigurationAudited buildConfigAudited,
            Set<BuildCoordinationStatus> states) {
        List<BuildTask> tasks = entityManager
                .createQuery(
                        "SELECT task from BuildTask task WHERE task.buildConfiguration.id = :buildConfigId "
                                + "AND task.buildConfigRev = :buildConfigRev AND task.status in :states",
                        BuildTask.class)
                .setParameter("buildConfigId", buildConfigAudited.getId())
                .setParameter("buildConfigRev", buildConfigAudited.getRev())
                .setParameter("states", states)
                .getResultList();

        switch (tasks.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(tasks.get(0));
            default:
                throw new IllegalStateException(
                        "Multiple build tasks enqueued for buildConfigAudited with id " + buildConfigAudited.getId());
        }
    }

    @Override
    public List<BuildTask> getBuildTasksInState(Set<BuildCoordinationStatus> states) {
        return entityManager
                .createQuery(
                        "SELECT task from BuildTask task WHERE task.status in :states order by task.id",
                        BuildTask.class)
                .setParameter("states", states)
                .getResultList();
    }

    @Override
    public Optional<BuildTask> getFirstTaskInState(BuildCoordinationStatus state) {
        return entityManager
                .createQuery(
                        "SELECT task from BuildTask task " + "WHERE task.status = :state order by task.id",
                        BuildTask.class)
                .setParameter("state", state)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) // mstodo verify this!
    public BuildTask grabTask(BuildTask buildTask) {
        int updatestRows = entityManager
                .createQuery(
                        "UPDATE BuildTask task SET task.processed = true "
                                + "WHERE task.id = :taskId AND task.status = :startState AND task.processed = false")
                .setParameter("startState", BuildCoordinationStatus.ENQUEUED)
                .setParameter("taskId", buildTask.getId())
                .executeUpdate();
        if (updatestRows == 1) {
            return entityManager.createQuery(
                    "SELECT task from BuildTask task  join fetch task.buildConfiguration bc join fetch bc.buildEnvironment env join fetch env.attributes "
                            + "where task.id = :id",
                    BuildTask.class).setParameter("id", buildTask.getId()).getSingleResult();
        } else {
            return null;
        }
    }

    @Override
    public long countTasksInState(Set<BuildCoordinationStatus> states) {
        return entityManager.createQuery("select count(t) from BuildTask t where t.status in :states", Long.class)
                .setParameter("states", states)
                .getSingleResult();
    }

    // mstodo
    @Override
    public List<BuildTask> getNewTasksWithDepsInStates(Set<BuildCoordinationStatus> states) {
        return entityManager
                .createQuery(
                        "select t from BuildTask t join t.dependencies d join BuildTask dep on dep.id = d "
                                + "where dep.status in :successful_finish_states and t.status = :task_state "
                                + "group by t.id " + "having count(dep) = 0",
                        BuildTask.class)
                .setParameter("task_state", BuildCoordinationStatus.NEW)
                .setParameter("successful_finish_states", states)
                .getResultList();
    }

    @Override
    public void transitionWaitingToReadyIfDepsBuilt() {
        entityManager
                .createQuery(
                        "update BuildTask t set t.status = :targetState where t.status = :currentState and 0 = ("
                                + "   select count(d) from t.dependencies d join BuildTask depTask on depTask.id = d " +
                                "where depTask.status in :unfinishedStates)")
                .setParameter("targetState", BuildCoordinationStatus.ENQUEUED)
                .setParameter("currentState", BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES)
                .setParameter("unfinishedStates", UNFINISHED_OR_FAILED_STATES)
                .executeUpdate();
    }

    @Override
    public BuildTask getTaskWithAllProperties(BuildTask task) {
        log.info("getting task with id {} from DB", task.getId()); // mstodo remove all log.infos from hjere
        List<BuildTask> resultList = entityManager.createQuery(
                "SELECT task from BuildTask task "
                        // + "join fetch task.buildConfiguration bc join fetch bc.buildEnvironment env "
                        // + "join fetch env.attributes left join fetch task.dependants left join fetch
                        // task.dependencies "
                        + "where task.id = :id",
                BuildTask.class).setParameter("id", task.getId()).getResultList();

        switch (resultList.size()) {
            case 0:
                return task; // it's before storing the task in the DB, no refresh possible
            case 1:
                return resultList.get(0);
            default:
                throw new IllegalStateException("Multiple tasks with id " + task.getId() + " found in the DB");
        }
    }

    @Override
    public boolean areDependenciesBuilt(BuildTask task) {
        return entityManager.createQuery("select count(d) from BuildTask t join t.dependencies d join BuildTask depTask on depTask.id = d " +
                                               "where depTask.status in :unfinishedStates", Integer.class)
                .getSingleResult() == 0;
    }

    @Override
    public List<BuildTask> getBuildTasksByBCSRId(Integer buildConfigSetRecordId) {
        return entityManager.createQuery(
                "select bt from BuildTask bt where bt.buildConfigSetRecord.id = :setId", BuildTask.class)
                .setParameter("setId", buildConfigSetRecordId)
                .getResultList();
    }

}
