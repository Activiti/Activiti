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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ServiceTaskParseHandlerTest {

    @InjectMocks
    private ServiceTaskParseHandler serviceTaskParseHandler;

    @Mock
    private BpmnParse bpmnParse;

    @Mock
    private ActivityBehaviorFactory activityBehaviorFactory;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        given(bpmnParse.getActivityBehaviorFactory())
            .willReturn(activityBehaviorFactory);
    }

    @Test
    public void executeParseShouldUseDefaultBehaviorWhenNoInformationIsProvided()
        throws Exception {
        //given
        ServiceTask serviceTask = new ServiceTask();
        ServiceTaskDelegateExpressionActivityBehavior defaultBehavior = mock(
            ServiceTaskDelegateExpressionActivityBehavior.class
        );
        given(
            activityBehaviorFactory.createDefaultServiceTaskBehavior(
                serviceTask
            )
        )
            .willReturn(defaultBehavior);

        //when
        serviceTaskParseHandler.executeParse(bpmnParse, serviceTask);

        //then
        assertThat(serviceTask.getBehavior()).isEqualTo(defaultBehavior);
    }
}
