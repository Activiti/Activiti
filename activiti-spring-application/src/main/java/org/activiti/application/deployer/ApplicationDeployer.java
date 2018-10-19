/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.application.deployer;

import java.util.List;

import org.activiti.application.ApplicationContent;
import org.activiti.application.ApplicationService;

public class ApplicationDeployer {

    private ApplicationService applicationLoader;

    private List<ApplicationEntryDeployer> deployers;

    public ApplicationDeployer(ApplicationService applicationLoader,
                               List<ApplicationEntryDeployer> deployers) {
        this.applicationLoader = applicationLoader;
        this.deployers = deployers;
    }

    public void deploy() {
        List<ApplicationContent> applications = applicationLoader.loadApplications();
        for (ApplicationContent application : applications) {
            for (ApplicationEntryDeployer deployer : deployers) {
                deployer.deployEntries(application);
            }
        }
    }

}
