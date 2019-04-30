/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.TaskResponse;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.restmodel.bpm.BpmNotificationRest;
import org.jboss.pnc.spi.exception.CoreException;

import java.util.function.Consumer;

public interface SCMRepositoryProvider extends Provider<RepositoryConfiguration, SCMRepository, SCMRepository> {

    Page<SCMRepository> getAllWithMatchAndSearchUrl(int pageIndex,
                                                    int pageSize,
                                                    String sortingRsql,
                                                    String query,
                                                    String matchUrl,
                                                    String searchUrl);

    TaskResponse createSCMRepositoryWithOneUrl(CreateAndSyncSCMRequest scmRequest,
            BuildConfiguration configuration,
            Consumer<BpmNotificationRest> onSuccessConsumer) throws CoreException;
}
