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
package org.jboss.pnc.coordinator.builder;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.runtime.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
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

    @Inject
    BuildCoordinator buildCoordinator;

    @Deprecated // CDI
    public SetRecordUpdateJob() {
    }

    /**
     * see {@link org.jboss.pnc.spi.coordinator.BuildSetTask#taskStatusUpdatedToFinalState(Consumer)} +
     * NO_REBUILD_REQUIRED
     * <p>
     * TODO: it would be good to handle NO_REBUILD_REQUIRED upfront, easier for Rex later
     */
    @Schedule(hour = "*", minute = "*", second = "0,10,20,30,40,50")
    @Transactional
    public void updateConfigSetRecordsStatuses() {
        log.debug("triggered the job");
        // #1 query for unfinished BCSR
        // for each -> BTasks -> check status
        //
        // see BTasks -> if BCSD NEW and running BTasks -> change to RUNNING
        // see BTasks -> if not BTasks -> check BRs -> decide final status
        // see BTasks ->
        List<BuildConfigSetRecord> setRecords = setRecordRepository.findBuildConfigSetRecordsInProgress();
        log.debug("BCSRs in progress: {}", setRecords);
        for (BuildConfigSetRecord setRecord : setRecords) {
            updateConfigSetRecordStatus(setRecord);
        }
    }

    private void updateConfigSetRecordStatus(BuildConfigSetRecord setRecord) {
        log.debug("Checking BuildConfigSetRecord[{}] for status update", setRecord.getId());
        List<BuildTask> buildTasks = taskDatastore.getBuildTasksByBCSRId(setRecord.getId());
        Set<BuildRecord> buildRecords = setRecord.getBuildRecords();

        BuildStatus effectiveState = getEffectiveState(setRecord, buildTasks, buildRecords);
        if (setRecord.getStatus() != effectiveState) {
            updateConfigSetRecordStatus(setRecord, effectiveState);
            // mstodo Probably more specific logging
            log.debug("BuildConfigSetRecord[{}] changes status to {}", setRecord, effectiveState);
        } else {
            log.debug("BuildConfigSetRecord[{}] didn't change its status", setRecord);
        }
    }

    // todo test that build task is removed after build record is created for the DB based solution
    private BuildStatus getEffectiveState(
            BuildConfigSetRecord setRecord,
            List<BuildTask> buildTasks,
            Set<BuildRecord> buildRecords) {
        if (buildTasks.isEmpty() && buildRecords.isEmpty()) {
            log.error(
                    "BuildConfigSetRecord[{}] has no tasks and no build records, setting status to {}",
                    setRecord,
                    BuildStatus.REJECTED);
            return BuildStatus.REJECTED;
        }
        Set<String> ids = buildRecords.stream()
                .map(BuildRecord::getId)
                .map(Base32LongID::getId)
                .collect(Collectors.toSet());

        List<BuildTask> effectiveBuildTasks = buildTasks.stream()
                .filter(task -> !ids.contains(task.getId()))
                .collect(Collectors.toList());

        Set<BuildStatus> buildStatuses = Stream.concat(
                buildRecords.stream().map(BuildRecord::getStatus),
                effectiveBuildTasks.stream().map(BuildTask::getStatus).map(BuildStatus::fromBuildCoordinationStatus))
                .collect(Collectors.toSet());

        return determineStatus(buildStatuses);
    }

    private void updateConfigSetRecordStatus(BuildConfigSetRecord setRecord, BuildStatus effectiveState) {
        buildCoordinator.updateBuildConfigSetRecordStatus(setRecord, effectiveState, "");
    }
}
