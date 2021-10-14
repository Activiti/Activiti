/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.test.regression;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;

/**
 * All tests that do not belong to any other test case, but test a supposedly working feature go
 * here.
 */
public class RegressionTest extends PluggableActivitiTestCase {

    // https://jira.codehaus.org/browse/ACT-1623
    // NPE when eventbased gateway is after referenced event
    public void testAct1623() throws Exception {

        // Deploy processes
        String deploymentId = repositoryService.createDeployment()
            .addClasspathResource("org/activiti/engine/test/regression/act1623-processOne.bpmn")
            .addClasspathResource("org/activiti/engine/test/regression/act1623-processTwo.bpmn")
            .deploy().getId();

        runtimeService.startProcessInstanceByKey("ProcessOne");

        //Quick fix for random test failure on CI
        Thread.sleep(2000);

        // Clean
        repositoryService.deleteDeployment(deploymentId, true);
    }

}
