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

package org.activiti.examples.runtime;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.junit.Ignore;

/**
 * This test shows that bpmn endless loop with activiti6 is not only fiction
 */
@Ignore
public class StandardAgendaFailingTest extends PluggableActivitiTestCase {

    @Ignore("Endless loop with the standard agenda implementation can run 'forever'.")
    @Deployment(resources = "org/activiti/examples/runtime/WatchDogAgendaTest-endlessloop.bpmn20.xml")
    public void ignoreStandardAgendaWithEndLessLoop() {
        this.runtimeService.startProcessInstanceByKey("endlessloop");
    }

}
