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

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FlowNodeTest {

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

    @Test
    public void hasOutgoingFlows_should_returnTrue_when_outgoingFlowsArePresent() {
        IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
        SequenceFlow sequenceFlow = new SequenceFlow();
        intermediateCatchEvent.setOutgoingFlows(Arrays.asList(sequenceFlow));
        assertThat(intermediateCatchEvent.hasOutgoingFlows()).isTrue();
    }

    @Test
    public void hasIncomingFlows_should_returnTrue_when_incomingFlowsArePresent() {
        ThrowEvent throwEvent = new ThrowEvent();
        SequenceFlow sequenceFlow = new SequenceFlow();
        throwEvent.setIncomingFlows(Arrays.asList(sequenceFlow));
        assertThat(throwEvent.hasIncomingFlows()).isTrue();
    }


    @Test
    public void hasOutgoingFlows_should_returnFalse_when_outgoingFlowsAreNotPresent() {
        IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
        intermediateCatchEvent.setOutgoingFlows(null);
        assertThat(intermediateCatchEvent.hasOutgoingFlows()).isFalse();
    }

    @Test
    public void hasIncomingFlows_should_returnFalse_when_incomingFlowsAreNotPresent() {
        ThrowEvent throwEvent = new ThrowEvent();
        throwEvent.setIncomingFlows(null);
        assertThat(throwEvent.hasIncomingFlows()).isFalse();
    }

    @Test
    public void hasOutgoingFlows_should_returnFalse_when_outgoingFlowsAreEmpty() {
        IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
        List<SequenceFlow> sequenceFlows = Arrays.asList();
        intermediateCatchEvent.setOutgoingFlows(sequenceFlows);
        assertThat(intermediateCatchEvent.hasOutgoingFlows()).isFalse();
    }

    @Test
    public void hasIncomingFlows_should_returnFalse_when_incomingFlowsAreEmpty() {
        ThrowEvent throwEvent = new ThrowEvent();
        List<SequenceFlow> sequenceFlows = Arrays.asList();
        throwEvent.setIncomingFlows(sequenceFlows);
        assertThat(throwEvent.hasIncomingFlows()).isFalse();
    }
}
