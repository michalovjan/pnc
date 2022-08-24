/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.coordinator.builder;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.runtime.BuildTask;
import org.jboss.pnc.spi.datastore.BuildTaskDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

//mstodo

/**
 * <h3>Build task queue.</h3>
 *
 * The queue consists of 5 collections:
 * <ul>
 * <li>tasksInProgress - set of tasks that are being executed at the moment</li>
 * <li>readyTasks - queue of tasks that are ready to be executed but are waiting for a free executor (and throttling
 * mechanism)</li>
 * <li>waitingTasksWithCallbacks - tasks waiting for a dependency. As soon as their dependencies are built, they are
 * moved to readyTasks. The waiting tasks are mapped to callbacks that are executed upon the transfer</li>
 * <li>unfinishedTasks - tasks either waiting, ready or in progress. This collection is introduced to fix the race
 * condition in {@link #take(Consumer)}, where a task is taken from readyTask, and later put into tasksInProgress and
 * the method cannot be synchronized</li>
 * </ul>
 *
 * The BuildQueue is MDC aware, the MDC values present in the thread context when the tasks is added are restored when
 * an operation is run on element using {@link #take(Consumer)} method.
 *
 * <p/>
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 4/18/16 Time: 12:47 PM
 */
@ApplicationScoped
public class DatabaseBackedBuildQueue implements BuildQueue {
    private static final Logger log = LoggerFactory.getLogger(DatabaseBackedBuildQueue.class);

    private static final Set<BuildCoordinationStatus> IN_PROGRESS_STATES = BuildCoordinationStatus.inProgressStates();
    private static final Set<BuildCoordinationStatus> SUCCESSFUL_FINISH_STATES = BuildCoordinationStatus
            .successfulFinishStates();
    private SystemConfig systemConfig;

    // private final Set<MDCAwareElement<BuildTask>> unfinishedTasks = new HashSet<>();

    // private final BlockingQueue<MDCAwareElement<BuildTask>> readyTasks = new LinkedBlockingQueue<>();
    // private final Map<MDCAwareElement<BuildTask>, Runnable> waitingTasksWithCallbacks = new HashMap<>();
    // private final Set<MDCAwareElement<BuildTask>> tasksInProgress = ConcurrentHashMap.newKeySet();

    private final Semaphore availableBuildSlots = new Semaphore(0);

    private BuildTaskDatastore datastore;

    @Inject
    public DatabaseBackedBuildQueue(SystemConfig systemConfig, BuildTaskDatastore datastore) {
        this.systemConfig = systemConfig;
        this.datastore = datastore;
    }

    private Consumer<BuildTask> onTaskReady;

    @SuppressWarnings("unused")
    @Deprecated
    public DatabaseBackedBuildQueue() {
    }

    /**
     * Add a new, ready to build task to queue
     *
     * @param task task to be enqueued
     */
    public boolean addReadyTask(BuildTask task) {
        if (!datastore.areDependenciesBuilt(task)) { // mstodo implement here
            throw new IllegalArgumentException("a not ready task added to the queue: " + task);
        }
        // MDCAwareElement element = new MDCAwareElement(task);
        datastore.persist(task);

        log.debug("adding task: {}", task);
        return true;
    }

    /**
     * Add a task that is waiting for dependencies
     *
     * @param task task that is not ready to build
     */
    @Override
    public void addWaitingTask(BuildTask task) {
        MDCAwareElement element = new MDCAwareElement(task);
        datastore.persist(task);
        log.debug("adding waiting task: {}", task);
    }

    /**
     * remove task from the queue. This method should be invoked if the task is completed, either successfully, or with
     * error (including rejected build)
     *
     * @param task task to be removed
     */
    @Override
    public void removeTask(BuildTask task) {
        log.debug("removing task: {}", task); // mstodo check how it's used?
        datastore.remove(task);
        availableBuildSlots.release();
    }

    /**
     * Trigger searching for ready tasks in the waiting queue. This method should be invoked if one task has finished
     * and there's a possibility that other tasks became ready to be built.
     */
    public synchronized void executeNewReadyTasks() {
        // mstodo just switch tasks to ready?
        // List<MDCAwareElement<BuildTask>> newReadyTasks = extractReadyTasks();
        log.debug("Ignoring starting new ready tasks.");
        List<BuildTask> newReadyTasks = datastore.getNewTasksWithDepsInStates(SUCCESSFUL_FINISH_STATES);
        for (BuildTask newReadyTask : newReadyTasks) {
            newReadyTask.setStatus(BuildCoordinationStatus.ENQUEUED);
            onTaskReady.accept(newReadyTask);
        }
    }

    /**
     * Get build task for given build systemConfig from the queue.
     *
     * @param buildConfigAudited build systemConfig
     * @return Optional.of(build task for the systemConfig) if build task is enqueued/in progress, Optional.empty()
     *         otherwise
     */
    public Optional<BuildTask> getTask(BuildConfigurationAudited buildConfigAudited) {
        return datastore.getTask(buildConfigAudited, IN_PROGRESS_STATES);
    }

    /**
     * List all waiting, ready and in progress tasks
     *
     * @return list of all build tasks in the queue
     */
    public List<BuildTask> getSubmittedBuildTasks() {
        return datastore.getBuildTasksInState(IN_PROGRESS_STATES);
    }

    private BuildTask take() throws InterruptedException {
        availableBuildSlots.acquire();
        log.info("Consumer is ready to go, waiting for task");
        while (true) {
            Optional<BuildTask> task = datastore.getFirstTaskInState(BuildCoordinationStatus.ENQUEUED);
            if (!task.isPresent()) {
                log.trace("Didn't get a task to start, let's wait and try again in a moment");
                // mstodo configurable wait
                Thread.sleep(50L); // no ready tasks found, let's take some rest
                // and try to find new ready tasks:
                datastore.transitionWaitingToReadyIfDepsBuilt();
            } else {
                log.debug("Got a task to start with id {}, let's try to lock it for starting", task.get().getId());
                BuildTask grabbedTask = datastore.grabTask(task.get());
                if (grabbedTask != null) {
                    log.debug("Successfully locked task with id {} to start", task.get().getId());

                    return grabbedTask;
                } // else take the next task
            }
        }
    }

    public void take(Consumer<BuildTask> consumer) throws InterruptedException {
        // Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        BuildTask task = take();
        log.info("Got task: {}, will start processing", task);
        consumer.accept(task);
        // Map<String, String> elementContextMap = element.getContextMap();
        // try {
        // if (elementContextMap != null) {
        // elementContextMap.forEach(MDC::put);
        // } else {
        // MDC.clear();
        // }
        // consumer.accept(element.get());
        // } finally {
        // if (elementContextMap != null) {
        // elementContextMap.keySet().forEach(MDC::remove);
        // }
        // // restore context
        // if (copyOfContextMap != null) {
        // MDC.setContextMap(copyOfContextMap);
        // }
        // }
    }

    @Override
    public void registerTaskReadyCallback(Consumer<BuildTask> onTaskReady) {
        this.onTaskReady = onTaskReady;
    }

    @Override
    public boolean isEmpty() {
        return datastore.countTasksInState(IN_PROGRESS_STATES) == 0;
    }

    @Override
    public String getDebugInfo() {
        StringBuilder result = new StringBuilder("=====================\nQUEUE STATE:\n=====================\n");
        List<BuildTask> unfinishedTasks = datastore.getBuildTasksInState(IN_PROGRESS_STATES);
        unfinishedTasks.sort(Comparator.comparing(BuildTask::getStatus));
        for (BuildTask task : getUnfinishedTasks()) {
            result.append('[').append(task.getStatus()).append(']').append(task);
        }
        return result.toString();
    }

    @Override
    public BuildTask refreshTask(BuildTask task) {
        return datastore.getTaskWithAllProperties(task);
    }

    @Override
    public boolean readyToBuild(BuildTask buildTask) {
        return false;
    }

    @Override
    public Collection<BuildTask> getDependencies(BuildTask task) {
        return task.getDependencies();
    }

    @Override
    public Collection<BuildTask> getBuildTasksByConfigSetRecordId(Integer buildConfigSetRecordId) {
        return datastore.getBuildTasksByBCSRId(buildConfigSetRecordId);
    }

    public Optional<BuildTask> getUnfinishedTask(BuildConfigurationAudited buildConfigurationAudited) {
        return getTask(buildConfigurationAudited);
        /*
         * Optional<BuildTask> task =
         * entityManager.createQuery("SELECT task from BuildTask task WHERE task.status = :state " + "order by task.id",
         * BuildTask.class) .setParameter("state", BuildTaskState.READY) .setMaxResults(1) .getResultStream()
         * .findFirst() return unfinishedTasks.stream() .map(MDCAwareElement::get) .filter(buildTask ->
         * buildTask.getBuildConfigurationAudited().equals(buildConfigurationAudited)) .findFirst();
         */
    }

    public Collection<BuildTask> getUnfinishedTasks() {
        return datastore.getBuildTasksInState(IN_PROGRESS_STATES);
    }

    @PostConstruct
    public void initSemaphore() {
        int maxConcurrentBuilds = systemConfig.getCoordinatorMaxConcurrentBuilds();
        availableBuildSlots.release(maxConcurrentBuilds);
    }

    // @Override
    // public synchronized String toString() {
    // return "BuildQueue{" + "readyTasks=" + readyTasks + ", waitingTasks=" + waitingTasksWithCallbacks
    // + ", tasksInProgress=" + tasksInProgress + ", taskSets=" + taskSets + '}';
    // }
    //
    // @Override
    // public String getDebugInfo() {
    // Collection<BuildTask> tasks = datastore.getAll();
    // String info = "=====================\nQUEUE STATE:\n=====================\n" + "Available build slots: "
    // + availableBuildSlots.availablePermits() + "\n" + "Queue length:" + availableBuildSlots.getQueueLength()
    // + "\n" + "\n=====================\nTASKS IN PROGRESS:\n=====================\n" +
    // tasks.stream().filter(t -> t.getStatus() == BuildCoordinationStatus.BUILDING).collect(Collectors.toList())
    // + "\n=====================\nREADY TASKS:\n=====================\n" +
    // tasks.stream().filter(t -> t.getStatus() == BuildCoordinationStatus.ENQUEUED).collect(Collectors.toList())
    // + "\n=====================\nWAITING TASKS:\n=====================\n"
    // + tasks.stream().filter(t -> t.getStatus() ==
    // BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES).collect(Collectors.toList())
    // + "\n=====================\nALL UNFINISHED TASKS:\n=====================\n" +
    // tasks.stream().filter(t -> !t.getStatus().isCompleted()).collect(Collectors.toList());
    //
    // return info;
    // }
}
