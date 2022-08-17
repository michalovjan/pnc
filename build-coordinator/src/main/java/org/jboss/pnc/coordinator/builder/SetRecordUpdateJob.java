package org.jboss.pnc.coordinator.builder;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Consumer;

@Singleton
public class SetRecordUpdateJob {

    @Inject
    private BuildQueue queue;

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
    void updateConfigSetRecordStatuses() {
        // #1 query for unfinished BCSR
        //      for each -> BTasks -> check status
        //
        // see BTasks -> if BCSD NEW and running BTasks -> change to RUNNING
        // see BTasks -> if not BTasks -> check BRs -> decide final status
        // see BTasks ->
    }
}
