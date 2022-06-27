/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.boot.test.util;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessCleanUpUtil {

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    public void cleanUpWithAdmin() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processes = processAdminRuntime.processInstances(Pageable.of(0,
                50));
        for (ProcessInstance processInstance : processes.getContent()) {
            if (processInstance.getParentId() == null) {
                processAdminRuntime.delete(ProcessPayloadBuilder
                                                   .delete()
                                                   .withProcessInstance(processInstance)
                                                   .withReason("test clean up")
                                                   .build());
            }
        }
    }
}
