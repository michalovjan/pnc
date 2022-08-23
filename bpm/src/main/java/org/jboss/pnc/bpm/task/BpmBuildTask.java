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
package org.jboss.pnc.bpm.task;

import lombok.ToString;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.bpm.model.ComponentBuildParameters;
import org.jboss.pnc.model.runtime.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author Jakub Senko
 */
@ToString(callSuper = true)
public class BpmBuildTask extends BpmTask {

    private final BuildTask buildTask;

    public BuildTask getBuildTask() {
        return buildTask;
    }

    public BpmBuildTask(BuildTask buildTask) {
        super(buildTask.getUser().getLoginToken());
        this.buildTask = buildTask;
    }

    @Override
    protected Serializable getProcessParameters() throws CoreException {
        return new ComponentBuildParameters(
                globalConfig.getPncUrl(),
                globalConfig.getExternalIndyUrl(),
                globalConfig.getExternalRepourUrl(),
                globalConfig.getExternalDaUrl(),
                Boolean.valueOf(Optional.ofNullable(config.getCommunityBuild()).orElse("true")),
                Boolean.valueOf(Optional.ofNullable(config.getVersionAdjust()).orElse("false")),
                getBuildExecutionConfiguration(buildTask));
    }

    private BuildExecutionConfigurationRest getBuildExecutionConfiguration(BuildTask buildTask) {
        throw new IllegalStateException("This functionality is no longer available");
    }

    @Override
    public String getProcessId() {
        throw new IllegalStateException("This functionality is no longer available");
    }
}
