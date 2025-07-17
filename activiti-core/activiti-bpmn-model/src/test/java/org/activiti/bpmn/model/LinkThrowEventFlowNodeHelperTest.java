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
package org.activiti.bpmn.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import org.activiti.bpmn.model.helper.LinkThrowEventFlowNodeHelper;
import org.junit.Test;

public class LinkThrowEventFlowNodeHelperTest {

    @Test
    public void findRelatedIntermediateCatchEventForLinkEvent_shouldReturnCorrectFlowNode_when_targetEventIsGiven_AndMatchingCatchEventIsFound() {
        Process process = new Process();
        process.setId("processId");

        ThrowEvent throwEvent = new ThrowEvent();
        LinkEventDefinition linkEventDefinition = new LinkEventDefinition();
        linkEventDefinition.setTarget("targetId");
        throwEvent.getEventDefinitions().add(linkEventDefinition);
        process.addFlowElement(throwEvent);

        IntermediateCatchEvent catchEvent = new IntermediateCatchEvent();
        LinkEventDefinition catchEventDefinition = new LinkEventDefinition();
        catchEventDefinition.setId("targetId");
        catchEvent.getEventDefinitions().add(catchEventDefinition);
        process.addFlowElement(catchEvent);

        FlowNode node = LinkThrowEventFlowNodeHelper.findRelatedIntermediateCatchEventForLinkEvent(throwEvent);

        assertThat(node).isEqualTo(catchEvent);
    }

    @Test
    public void findRelatedIntermediateCatchEventForLinkEvent_shouldReturnCorrectFlowNode_when_targetEventIsGiven_noMatchIsFound() {
        Process process = new Process();
        process.setId("processId");

        ThrowEvent throwEvent = new ThrowEvent();
        LinkEventDefinition linkEventDefinition = new LinkEventDefinition();
        linkEventDefinition.setTarget("targetId");
        throwEvent.getEventDefinitions().add(linkEventDefinition);
        process.addFlowElement(throwEvent);

        IntermediateCatchEvent catchEvent = new IntermediateCatchEvent();
        LinkEventDefinition catchEventDefinition = new LinkEventDefinition();
        catchEventDefinition.setId("otherId");
        catchEvent.getEventDefinitions().add(catchEventDefinition);
        process.addFlowElement(catchEvent);

        FlowNode node = LinkThrowEventFlowNodeHelper.findRelatedIntermediateCatchEventForLinkEvent(throwEvent);

        assertThat(node).isNull();
    }
}
