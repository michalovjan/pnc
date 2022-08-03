package org.jboss.pnc.coordinator.builder;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.coordinator.BuildTask;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface BuildQueue {
    Collection<BuildTask> getUnfinishedTasks();

    Optional<BuildTask> getUnfinishedTask(BuildConfigurationAudited buildConfigurationAudited);

    boolean addReadyTask(BuildTask buildTask);

    void addWaitingTask(BuildTask buildTask);

    void removeTask(BuildTask task);

    void executeNewReadyTasks();

    List<BuildTask> getSubmittedBuildTasks();

    /**
     * We may need a different API for it
     * @param buildTaskConsumer
     * @throws InterruptedException
     */
    void take(Consumer<BuildTask> buildTaskConsumer) throws InterruptedException;

    void registerTaskReadyCallback(Consumer<BuildTask> onTaskReady);
}
