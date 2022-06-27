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

package org.activiti.engine.impl.bpmn.parser.factory;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EventListener;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.CustomPropertiesResolver;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;
import org.activiti.engine.delegate.TransactionDependentTaskListener;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * Factory class used by the {@link BpmnParser} and {@link BpmnParse} to instantiate the behaviour classes for {@link TaskListener} and {@link ExecutionListener} usages.
 *
 * You can provide your own implementation of this class. This way, you can give different execution semantics to the standard construct.
 *
 * The easiest and advisable way to implement your own {@link ListenerFactory} is to extend the {@link DefaultListenerFactory}.
 *
 * An instance of this interface can be injected in the {@link ProcessEngineConfigurationImpl} and its subclasses.
 *
 */
@Internal
public interface ListenerFactory {

  public abstract TaskListener createClassDelegateTaskListener(ActivitiListener activitiListener);

  public abstract TaskListener createExpressionTaskListener(ActivitiListener activitiListener);

  public abstract TaskListener createDelegateExpressionTaskListener(ActivitiListener activitiListener);

  public abstract ExecutionListener createClassDelegateExecutionListener(ActivitiListener activitiListener);

  public abstract ExecutionListener createExpressionExecutionListener(ActivitiListener activitiListener);

  public abstract ExecutionListener createDelegateExpressionExecutionListener(ActivitiListener activitiListener);

  public abstract TransactionDependentExecutionListener createTransactionDependentDelegateExpressionExecutionListener(ActivitiListener activitiListener);

  public abstract ActivitiEventListener createClassDelegateEventListener(EventListener eventListener);

  public abstract ActivitiEventListener createDelegateExpressionEventListener(EventListener eventListener);

  public abstract ActivitiEventListener createEventThrowingEventListener(EventListener eventListener);

  public abstract CustomPropertiesResolver createClassDelegateCustomPropertiesResolver(ActivitiListener activitiListener);

  public abstract CustomPropertiesResolver createExpressionCustomPropertiesResolver(ActivitiListener activitiListener);

  public abstract CustomPropertiesResolver createDelegateExpressionCustomPropertiesResolver(ActivitiListener activitiListener);

  public abstract TransactionDependentTaskListener createTransactionDependentDelegateExpressionTaskListener(ActivitiListener activitiListener);
}
