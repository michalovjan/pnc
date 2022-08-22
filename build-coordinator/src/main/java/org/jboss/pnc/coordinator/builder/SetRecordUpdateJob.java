package org.jboss.pnc.coordinator.builder;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.runtime.BuildTask;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.datastore.BuildTaskDatastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class SetRecordUpdateJob {

    @Inject
    private BuildTaskDatastore taskDatastore;

    @Inject
    BuildConfigSetRecordRepository setRecordRepository;

    @Inject
    BuildCoordinator buildCoordinator;

    @Deprecated //CDI
    public SetRecordUpdateJob() {
    }

    /**
     * see {@link org.jboss.pnc.spi.coordinator.BuildSetTask#taskStatusUpdatedToFinalState(Consumer)}
     * + NO_REBUILD_REQUIRED
     * <p>
     * TODO: it would be good to handle NO_REBUILD_REQUIRED upfront, easier for Rex later
     */
    @Schedule(hour = "*", minute = "*", second = "0,10,20,30,40,50")
    @Transactional
    void updateConfigSetRecordsStatuses() {
        log.debug("triggered the job");
        // #1 query for unfinished BCSR
        //      for each -> BTasks -> check status
        //
        // see BTasks -> if BCSD NEW and running BTasks -> change to RUNNING
        // see BTasks -> if not BTasks -> check BRs -> decide final status
        // see BTasks ->
        List<BuildConfigSetRecord> setRecords = setRecordRepository.findBuildConfigSetRecordsInProgress();

        for (BuildConfigSetRecord setRecord : setRecords) {
            updateConfigSetRecordStatus(setRecord);
        }
    }

    private void updateConfigSetRecordStatus(BuildConfigSetRecord setRecord) {
        log.debug("Checking BuildConfigSetRecord[{}] for status update", setRecord.getId());
        List<BuildTask> buildTasks = taskDatastore.getBuildTasksByBCSRId(setRecord.getId());
        Set<BuildRecord> buildRecords = setRecord.getBuildRecords();

        BuildStatus effectiveState = getEffectiveState(buildTasks, buildRecords);
        if (setRecord.getStatus() != effectiveState) {
            updateConfigSetRecordStatus(setRecord, effectiveState);
            // mstodo Probably more specific logging
            log.debug("BuildConfigSetRecord[{}] changes status to", effectiveState);
        } else {
            log.debug("BuildConfigSetRecord[{}] didn't change its status", setRecord.getId());
        }
    }

    //todo test that build task is removed after build record is created for the DB based solution
    private BuildStatus getEffectiveState(List<BuildTask> buildTasks, Set<BuildRecord> buildRecords) {
    }

    private void updateConfigSetRecordStatus(BuildConfigSetRecord setRecord, BuildStatus effectiveState) {
        buildCoordinator.updateBuildConfigSetRecordStatus(setRecord, effectiveState, "");
    }

    // mstodo
    private void completeBuildSetTask(BuildConfigSetRecord record) {
        log.debug("Completing buildSetTask {} ...", record);

        buildSetTask.taskStatusUpdatedToFinalState(status -> {
            if (BuildStatus.NO_REBUILD_REQUIRED == record.getStatus() && status == BuildStatus.SUCCESS) {
                log.debug("Build set already marked as NO_REBUILD_REQUIRED. BuildSetTask: {}", this);
            } else {
                log.debug("Marking build set as SUCCESS. BuildSetTask: {}", this);
                record.setStatus(BuildStatus.SUCCESS);
            }
            record.setStatus(status);
            record.setEndTime(new Date());
        });
        //TODO REMOVE
        updateBuildSetTaskStatus(buildSetTask, BuildSetStatus.DONE);

        buildSetTask.getBuildConfigSetRecord().ifPresent(r -> {
            try {
                datastoreAdapter.saveBuildConfigSetRecord(r);
            } catch (DatastoreException e) {
                log.error("Unable to save build config set record", e);
            }
        });
    }

    /*
            // TODO MOVE TO JOB
        Integer buildSetTaskId = task.getBuildConfigSetRecordId();
        BuildSetTask buildSetTask = buildQueue.getBuildSetTask(buildSetTaskId);
        if (buildSetTask != null && buildSetTask.isFinished()) {
            completeBuildSetTask(buildSetTask);
        } else if (buildSetTask != null) {
            // mstodo remove maybe?
            log.debug(
                    "build set task not finished yet, builds: \n\t{}",
                    buildSetTask.getBuildTasks()
                            .stream()
                            .sorted(Comparator.comparing(BuildTask::getStatus))
                            .map(t -> String.format("%s: [%s]", t.getId(), t.getStatus()))
                            .collect(Collectors.joining("\n\t")));
        }
     */
}
