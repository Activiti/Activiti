/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.impl;

import java.util.List;

import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.process.ProcessVariablesInitiator;

/**
 * Default implementation of the {@link ActivityBehaviorFactory}. Used when no custom {@link ActivityBehaviorFactory} is injected on the {@link ProcessEngineConfigurationImpl}.
 */
public class MappingAwareActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    private VariablesMappingProvider variablesMappingProvider;

    private ProcessVariablesInitiator processVariablesInitiator;

    public MappingAwareActivityBehaviorFactory(VariablesMappingProvider variablesMappingProvider,
                                               ProcessVariablesInitiator processVariablesInitiator) {
        super();
        this.variablesMappingProvider = variablesMappingProvider;
        this.processVariablesInitiator = processVariablesInitiator;
        
        this.setMessagePayloadMappingProviderFactory(new JsonMessagePayloadMappingProviderFactory(variablesMappingProvider));
    }

    @Override
    public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {
        return new MappingAwareUserTaskBehavior(userTask,
                                                variablesMappingProvider);
    }

    @Override
    protected CallActivityBehavior createCallActivityBehavior(Expression expression, List<MapExceptionEntry> mapExceptions) {
        return new MappingAwareCallActivityBehavior(expression,
                                                    mapExceptions,
                                                    variablesMappingProvider,
                                                    processVariablesInitiator);
    }

    @Override
    protected CallActivityBehavior createCallActivityBehavior(String calledElement,
                                                              List<MapExceptionEntry> mapExceptions) {
        return new MappingAwareCallActivityBehavior(calledElement,
                                                    mapExceptions,
                                                    variablesMappingProvider,
                                                    processVariablesInitiator);
    }
}
