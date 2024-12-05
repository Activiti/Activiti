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
package org.activiti.engine.test.bpmn.event.link;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.validation.validator.Problems;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LinkEventValidationTest extends PluggableActivitiTestCase {

    public void testLinkEventsWithoutTarget() {
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/link/LinkEventTests.noTarget.bpmn20.xml")
                .deploy())
            .withMessageContaining(Problems.LINK_EVENT_DEFINITION_MISSING_TARGET);
    }

    public void testLinkEventsWithoutSource() {
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/link/LinkEventTests.noSource.bpmn20.xml")
                .deploy())
            .withMessageContaining(Problems.LINK_EVENT_DEFINITION_MISSING_SOURCE);
    }

    public void testLinkEventsWithoutSourceAndTarget() {
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/link/LinkEventTests.noSourceAndTarget.bpmn20.xml")
                .deploy())
            .withMessageContaining(Problems.LINK_EVENT_DEFINITION_MISSING_TARGET, Problems.LINK_EVENT_DEFINITION_MISSING_SOURCE);
    }

}
