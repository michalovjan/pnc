package org.jboss.pnc.coordinator.builder;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.runtime.BuildTask;
import org.jboss.pnc.spi.datastore.BuildTaskDatastore;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class SetRecordUpdateJob {


    @Inject
    private BuildTaskDatastore taskDatastore;

    @Inject
    BuildConfigSetRecordRepository setRecordRepository;

    @Deprecated //CDI
    public SetRecordUpdateJob() {
    }

    /**
     * see {@link org.jboss.pnc.spi.coordinator.BuildSetTask#taskStatusUpdatedToFinalState(Consumer)}
     * + NO_REBUILD_REQUIRED
     *
     * TODO: it would be good to handle NO_REBUILD_REQUIRED upfront, easier for Rex later
     */
    @Schedule(hour = "*", minute = "*", second = "0,5,10,15,20,25,30,35,40,45,50,55")
    @Transactional
    void updateConfigSetRecordStatuses() {
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
        List<BuildTask> buildTasks = taskDatastore.getBuildTasksByBCSRId(setRecord.getId());
        Set<BuildRecord> buildRecords = setRecord.getBuildRecords();

        BuildStatus effectiveState = getEffectiveState(buildTasks, buildRecords);
        if (setRecord.getStatus() != effectiveState) {
            updateConfigSetRecordStatus(setRecord, effectiveState);
        }
    }

    private BuildStatus getEffectiveState(List<BuildTask> buildTasks, Set<BuildRecord> buildRecords) {
    }

    private void updateConfigSetRecordStatus(BuildConfigSetRecord setRecord, BuildStatus effectiveState) {

    }
}
