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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FlowNodeTest{

    @Test
    public void isLinkCatchEvent_should_returnTrue_when_intermediateCatch_withLinkEventDefinition() {
        IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
        LinkEventDefinition linkEventDefinition = new LinkEventDefinition();
        intermediateCatchEvent.addEventDefinition(linkEventDefinition);

        assertThat(intermediateCatchEvent.isLinkCatchEvent()).isTrue();
    }

    @Test
    public void isLinkCatchEvent_should_returnFalse_when_notIntermediateCatch_withLinkEventDefinition() {
        ThrowEvent throwEvent = new ThrowEvent();
        LinkEventDefinition linkEventDefinition = new LinkEventDefinition();
        throwEvent.addEventDefinition(linkEventDefinition);

        assertThat(throwEvent.isLinkCatchEvent()).isFalse();
    }

    @Test
    public void isLinkThrowEvent_should_returnTrue_when_throwEvent_withLinkEventDefinition() {
        ThrowEvent throwEvent = new ThrowEvent();
        LinkEventDefinition linkEventDefinition = new LinkEventDefinition();
        throwEvent.addEventDefinition(linkEventDefinition);

        assertThat(throwEvent.isLinkThrowEvent()).isTrue();
    }

    @Test
    public void isLinkThrowEvent_should_returnFalse_when_notThrowEvent_withLinkEventDefinition() {
        IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
        LinkEventDefinition linkEventDefinition = new LinkEventDefinition();
        intermediateCatchEvent.addEventDefinition(linkEventDefinition);

        assertThat(intermediateCatchEvent.isLinkThrowEvent()).isFalse();
    }

    @Test
    public void isInitialFlowNode_should_returnTrue_when_startEvent() {
        StartEvent startEvent = new StartEvent();
        assertThat(startEvent.isInitialFlowNode()).isTrue();
    }

    @Test
    public void isInitialFlowNode_should_returnFalse_when_linkCatchEvent() {
        IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
        LinkEventDefinition linkEventDefinition = new LinkEventDefinition();
        intermediateCatchEvent.addEventDefinition(linkEventDefinition);

        assertThat(intermediateCatchEvent.isInitialFlowNode()).isFalse();
    }
}
