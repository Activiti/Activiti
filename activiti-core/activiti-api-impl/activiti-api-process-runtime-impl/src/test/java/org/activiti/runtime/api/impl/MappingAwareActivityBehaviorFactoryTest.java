/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.impl;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.Expression;
import org.junit.jupiter.api.Test;

public class MappingAwareActivityBehaviorFactoryTest {

    private MappingAwareActivityBehaviorFactory factory = new MappingAwareActivityBehaviorFactory(null,
                                                                                                  null);

    @Test
    public void createUserTaskActivityBehaviorShouldReturnMappingAwareUserTaskBehavior() {
        assertThat(factory.createUserTaskActivityBehavior(mock(UserTask.class)))
                .isInstanceOf(MappingAwareUserTaskBehavior.class);
    }

    @Test
    public void createCallActivityBehaviorShouldReturnMappingAwareCallActivityBehavior() {
        assertThat(factory.createCallActivityBehavior("element", emptyList()))
                .isInstanceOf(MappingAwareCallActivityBehavior.class);
    }

    @Test
    public void createCallActivityBehaviorWithExpressionShouldReturnMappingAwareCallActivityBehavior() {
        assertThat(factory.createCallActivityBehavior(mock(Expression.class), emptyList()))
                .isInstanceOf(MappingAwareCallActivityBehavior.class);
    }

    @Test
    public void getMessagePayloadMappingProviderFactoryShouldReturnJsonMessagePayloadMappingProvider() {
        assertThat(factory.getMessagePayloadMappingProviderFactory())
                .isInstanceOf(JsonMessagePayloadMappingProviderFactory.class);
    }

}
