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
package org.jboss.pnc.mock.datastore;

import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.BuildTaskDatastore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class BuildTaskDatastoreMock implements BuildTaskDatastore {
    private final AtomicLong taskIds = new AtomicLong(1L);
    private final Map<String, BuildTask> tasks = new ConcurrentHashMap<>();

    @Override
    public void persist(BuildTask task) {
        task.setId("" + taskIds.getAndIncrement());
        tasks.put(task.getId(), task);
    }

    @Override
    public void remove(BuildTask task) {
        tasks.remove(task.getId());
    }

    @Override
    public Optional<BuildTask> getTask(
            BuildConfigurationAudited buildConfigAudited,
            Set<BuildCoordinationStatus> states) {
        List<BuildTask> tasks = this.tasks.values()
                .stream()
                .filter(
                        t -> states.contains(t.getStatus())
                                && t.getBuildConfigurationAudited().getId().equals(buildConfigAudited.getId()))
                .collect(Collectors.toList());
        switch (tasks.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(tasks.get(0));
            default:
                throw new IllegalStateException(
                        "Multiple tasks in states " + states + " found for build config with id "
                                + buildConfigAudited.getId());
        }
    }

    @Override
    public List<BuildTask> getBuildTasksInState(Set<BuildCoordinationStatus> states) {
        return this.tasks.values().stream().filter(t -> states.contains(t.getStatus())).collect(Collectors.toList());
    }

    @Override
    public Optional<BuildTask> getFirstTaskInState(BuildCoordinationStatus state) {
        return getBuildTasksInState(EnumSet.of(state)).stream().min(Comparator.comparing(BuildTask::getId));
    }

    @Override
    // synchronized should work because we are in a single JVM, using a single BuildTaskDatastoreMock object
    public synchronized BuildTask grabTask(BuildTask buildTask) {
        BuildTask freshTask = tasks.get(buildTask.getId());
        if (freshTask.getStatus() == BuildCoordinationStatus.ENQUEUED && !freshTask.isProcessed()) {
            freshTask.setProcessed(true);
            return freshTask;
        } else {
            return null;
        }
    }

    @Override
    public long countTasksInState(Set<BuildCoordinationStatus> states) {
        return getBuildTasksInState(states).size();
    }

    @Override
    public List<BuildTask> getNewTasksWithDepsInStates(Set<BuildCoordinationStatus> states) {
        List<BuildTask> result = new ArrayList<>();
        for (BuildTask task : tasks.values()) {
            if (task.getStatus() == BuildCoordinationStatus.NEW) {
                boolean hasAllDepsFinishedSuccessfully = true;
                for (BuildTask dependency : task.getDependencies()) {
                    BuildCoordinationStatus depStatus = dependency.getStatus();
                    if (!states.contains(depStatus)) {
                        hasAllDepsFinishedSuccessfully = false;
                    }
                }
                if (hasAllDepsFinishedSuccessfully) {
                    result.add(task);
                }
            }
        }
        return result;
    }

    @Override
    public void transitionWaitingToReadyIfDepsBuilt() {
        for (BuildTask task : tasks.values()) {
            if (task.getStatus() == BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES) {
                boolean hasUnfinishedOrFailedDep = false;
                for (BuildTask dependency : task.getDependencies()) {
                    BuildCoordinationStatus depState = dependency.getStatus();
                    if (!depState.isCompleted()) {
                        hasUnfinishedOrFailedDep = true;
                    } else if (depState.hasFailed()) {
                        hasUnfinishedOrFailedDep = true;
                    }
                }
                if (!hasUnfinishedOrFailedDep) {
                    // mstodo we have no even to transition to enqueued with the db one!
                    task.setStatus(BuildCoordinationStatus.ENQUEUED);
                }
            }
        }
    }

    @Override
    public BuildTask getTaskWithAllProperties(BuildTask task) {
        return tasks.get(task.getId());
    }
}
