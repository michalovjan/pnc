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
package org.jboss.pnc.model.runtime;

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
@Entity
@Table(indexes = @Index(name = "build_task_status_idx", columnList = "status"))
public class BuildTask {

    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.build-task");

    @Id
    @Setter
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    private BuildConfiguration buildConfiguration; // TODO decouple DB entity
    private Integer buildConfigRev;

    @Getter
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private BuildOptions buildOptions;

    @ManyToOne
    private User user;
    private Date submitTime;

    private String contentId;

    private Date startTime;
    private Date endTime;

    @Enumerated(EnumType.STRING)
    private BuildCoordinationStatus status = BuildCoordinationStatus.NEW;

    /**
     * if true, the task has been taken from the ENQUEUED state tasks to be stared used by the DB backed build queue to
     * ensure the task is "consumed" only once even in clustered deployment
     */
    @Getter
    @Setter
    private boolean processed;
    private String statusDescription;

    /**
     * A list of builds waiting for this build to complete.
     */

    // @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.EAGER)

    @ManyToMany
    private final Set<BuildTask> dependants = new HashSet<>();

    /**
     * The builds which must be completed before this build can start
     */
    @ManyToMany(mappedBy = "dependants", fetch = FetchType.EAGER)
    // @ElementCollection
    private Set<BuildTask> dependencies = new HashSet<>();

    @ManyToOne
    @Getter
    private BuildConfigSetRecord buildConfigSetRecord; // mstodo

    @ManyToOne
    private ProductMilestone productMilestone;

    private boolean hasFailed = false;

    /**
     * This BR is set when Build Task is not required to be built.
     */
    @ManyToOne
    private BuildRecord noRebuildCause;

    /**
     * Request that started the builds
     */
    private String requestContext;

    @Transient
    @Getter
    @Setter
    private BuildConfigurationAudited buildConfigurationAudited; // mstodo initialize it

    @Deprecated // to make JPA happy
    public BuildTask() {
    }

    private BuildTask(
            BuildConfigurationAudited buildConfigurationAudited,
            BuildOptions buildOptions,
            User user,
            Date submitTime,
            String id,
            BuildConfigSetRecord buildConfigSetRecord,
            ProductMilestone productMilestone,
            String contentId,
            Optional<String> requestContext) {

        this.id = id;
        this.buildConfiguration = buildConfigurationAudited.getBuildConfiguration();
        this.buildConfigRev = buildConfigurationAudited.getRev();
        this.buildOptions = buildOptions;
        this.user = user;
        this.submitTime = submitTime;

        this.buildConfigSetRecord = buildConfigSetRecord;
        this.productMilestone = productMilestone;
        this.contentId = contentId;

        this.requestContext = requestContext.orElse(null);

        this.buildConfigurationAudited = buildConfigurationAudited;
    }

    public void setStatus(BuildCoordinationStatus status) {
        this.status = status;
        setHasFailed(status.hasFailed());
    }

    public ProductMilestone getProductMilestone() {
        return productMilestone;
    }

    public Set<BuildTask> getDependencies() {
        return dependencies;
    }

    public void addDependency(BuildTask buildTask) {
        if (!dependencies.contains(buildTask)) {
            dependencies.add(buildTask);
            buildTask.dependants.add(this);
        }
    }

    public Set<BuildTask> getDependants() {
        return dependants;
    }

    /**
     * @return current status
     */
    public BuildCoordinationStatus getStatus() {
        return status;
    }

    /**
     * @return Description of current status. Eg. WAITING: there is no available executor; FAILED: exceptionMessage
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * Check if this build task has a build configuration dependency on the given build task. The search include
     * transitive dependencies.
     *
     * @param buildTask The buildTask with the config to check
     * @return true if this task's build config has a dependency (including transitive) on the build config of the given
     *         task, otherwise false
     */
    public boolean hasConfigDependencyOn(BuildTask buildTask) {
        if (buildTask == null || this.equals(buildTask)) {
            return false;
        }

        if (buildConfiguration == null || buildConfiguration.getAllDependencies() == null) {
            return false;
        }

        return buildConfiguration.dependsOn(buildTask.getBuildConfigurationAudited().getBuildConfiguration());
    }

    /**
     * Check if this build task has a direct build configuration dependency on the given build task
     *
     * @param buildTask The buildTask with the config to check
     * @return true if this task's build config has a direct dependency on the build config of the given task, otherwise
     *         false
     */
    public boolean hasDirectConfigDependencyOn(BuildTask buildTask) {
        if (buildTask == null || this.equals(buildTask)) {
            return false;
        }

        if (buildConfiguration == null || buildConfiguration.getDependencies() == null) {
            return false;
        }

        return buildConfiguration.getDependencies()
                .contains(buildTask.getBuildConfigurationAudited().getBuildConfiguration());
    }

    public Optional<String> getRequestContext() {
        return Optional.ofNullable(requestContext);
    }

    /**
     * A build task is equal to another build task if they are using the same build configuration ID and version.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildTask buildTask = (BuildTask) o;

        return getBuildConfigurationAudited().equals(buildTask.getBuildConfigurationAudited());
    }

    @Override
    public int hashCode() {
        return getBuildConfigurationAudited().hashCode();
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public boolean hasFailed() {
        return this.hasFailed;
    }

    void setHasFailed(boolean hasFailed) {
        this.hasFailed = hasFailed;
    }

    public String getId() {
        return id;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public User getUser() {
        return user;
    }

    public Integer getBuildConfigSetRecordId() {
        return buildConfigSetRecord.getId();
    }
    /*
     * mstodo remove
     */

    /**
     * Check if this build is ready to build, for example if all dependency builds are complete.
     *
     * @return true if already built, false otherwise
     *//*
        * public boolean readyToBuild() { for (String buildTask : dependencies) { if
        * (!buildTask.getStatus().isCompleted()) { return false; } } return true; }
        */
    @Override
    public String toString() {
        return "Build Task id:" + id + ", name: " + getBuildConfigurationAudited().getName() + ", project name: "
                + getBuildConfigurationAudited().getProject().getName() + ", status: " + status;
    }

    public static BuildTask build(
            BuildConfigurationAudited buildConfigurationAudited,
            BuildOptions buildOptions,
            User user,
            String buildTaskId,
            BuildConfigSetRecord buildConfigSetRecord,
            Date submitTime,
            ProductMilestone productMilestone,
            String contentId,
            Optional<String> requestContext) {

        ProductMilestone milestone = productMilestone;
        if (milestone != null && milestone.getEndDate() != null) {
            userLog.warn(
                    "Not using current milestone {} for build task {}, because the milestone is closed.",
                    productMilestone,
                    buildTaskId);
            milestone = null;
        }

        return new BuildTask(
                buildConfigurationAudited,
                buildOptions,
                user,
                submitTime,
                buildTaskId,
                buildConfigSetRecord,
                milestone,
                contentId,
                requestContext);
    }

    public String getContentId() {
        return contentId;
    }

    public BuildRecord getNoRebuildCause() {
        return noRebuildCause;
    }

    public void setNoRebuildCause(BuildRecord noRebuildCause) {
        this.noRebuildCause = noRebuildCause;
    }
}
