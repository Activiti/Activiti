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
package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.LinkEventDefinition;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchLinkEventActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class LinkEventDefinitionParserHandlerTest {
    @InjectMocks
    private LinkEventDefinitionParseHandler linkEventDefinitionParseHandler;

    @Mock
    private BpmnParse bpmnParse;

    @Mock
    private ActivityBehaviorFactory activityBehaviorFactory;


    @Before
    public void setUp() throws Exception {
        given(bpmnParse.getActivityBehaviorFactory()).willReturn(activityBehaviorFactory);
    }

    @Test
    public void shouldSetLinkEventBehavior_whenExecuteParse() throws Exception {
        IntermediateCatchEvent catchEvent = new IntermediateCatchEvent();
        LinkEventDefinition linkEventDefinition = new LinkEventDefinition();
        catchEvent.getEventDefinitions().add(linkEventDefinition);
        given(bpmnParse.getCurrentFlowElement()).willReturn(catchEvent);
        given(activityBehaviorFactory.createIntermediateCatchLinkEventActivityBehavior(eq(catchEvent), eq(linkEventDefinition))).willReturn(mock(IntermediateCatchLinkEventActivityBehavior.class));

        linkEventDefinitionParseHandler.executeParse(bpmnParse,linkEventDefinition );

        assertThat(catchEvent.getBehavior()).isNotNull();
        assertThat(catchEvent.getBehavior()).isInstanceOf(IntermediateCatchLinkEventActivityBehavior.class);
    }
}
