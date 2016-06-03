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
package org.activiti.engine.impl.bpmn.listener;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.HasExecutionListeners;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.engine.delegate.BaseExecutionListener;
import org.activiti.engine.delegate.CustomPropertiesResolver;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;

/**
 * @author Joram Barrez
 */
public class ListenerUtil {
  
  public static void executeExecutionListeners(HasExecutionListeners elementWithExecutionListeners, DelegateExecution execution, String eventType) {
    List<ActivitiListener> listeners = elementWithExecutionListeners.getExecutionListeners();
    if (listeners != null && listeners.size() > 0) {
      ListenerFactory listenerFactory = Context.getProcessEngineConfiguration().getListenerFactory();
      for (ActivitiListener activitiListener : listeners) {

        if (eventType.equals(activitiListener.getEvent())) {

          BaseExecutionListener executionListener = null;

          if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createClassDelegateExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            if (activitiListener.getOnTransaction() != null) {
              executionListener = listenerFactory.createTransactionDependentDelegateExpressionExecutionListener(activitiListener);
            } else {
              executionListener = listenerFactory.createDelegateExpressionExecutionListener(activitiListener);
            }
          } else if (ImplementationType.IMPLEMENTATION_TYPE_INSTANCE.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = (ExecutionListener) activitiListener.getInstance();
          }

          if (executionListener != null) {
            if (activitiListener.getOnTransaction() != null) {
              planTransactionDependentExecutionListener(listenerFactory, execution, (TransactionDependentExecutionListener) executionListener, activitiListener);
            } else {
              execution.setEventName(eventType); // eventName is used to differentiate the event when reusing an execution listener for various events
              execution.setCurrentActivitiListener(activitiListener);
              ((ExecutionListener) executionListener).notify(execution);
              execution.setEventName(null);
              execution.setCurrentActivitiListener(null);
            }
          }
        }
      }
    }
  }

  protected static void planTransactionDependentExecutionListener(ListenerFactory listenerFactory, DelegateExecution execution, TransactionDependentExecutionListener executionListener, ActivitiListener activitiListener) {
    TransactionDependentListeners executionListenerContextCloseListener = null;

    for (CommandContextCloseListener commandContextCloseListener : Context.getCommandContext().getCloseListeners()) {
      if (commandContextCloseListener instanceof TransactionDependentListeners) {
        executionListenerContextCloseListener = (TransactionDependentListeners) commandContextCloseListener;
        break;
      }
    }

    if (executionListenerContextCloseListener == null) {
      executionListenerContextCloseListener = new TransactionDependentListeners();
      Context.getCommandContext().addCloseListener(executionListenerContextCloseListener);
    }

    // current state of the execution variables will be stored
    Map<String, Object> executionVariablesToUse = execution.getVariables();

    // create custom properties resolver
    CustomPropertiesResolver customPropertiesResolver = null;
    if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getCustomPropertiesResolverImplementationType())) {
      customPropertiesResolver = listenerFactory.createClassDelegateCustomPropertiesResolver(activitiListener);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getCustomPropertiesResolverImplementationType())) {
      customPropertiesResolver = listenerFactory.createExpressionCustomPropertiesResolver(activitiListener);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getCustomPropertiesResolverImplementationType())) {
      customPropertiesResolver = listenerFactory.createDelegateExpressionCustomPropertiesResolver(activitiListener);
    }

    // invoke custom properties resolver
    Map<String, Object> customPropertiesMapToUse = null;
    if (customPropertiesResolver != null) {
      customPropertiesMapToUse = customPropertiesResolver.getCustomPropertiesMap(execution);
    }

    // add to context close listener stack
    if (TransactionDependentExecutionListener.ON_TRANSACTION_BEFORE_COMMIT.equals(activitiListener.getOnTransaction())) {
      executionListenerContextCloseListener.addClosingExecutionListener(executionListener, execution, executionVariablesToUse, customPropertiesMapToUse);
    } else if (TransactionDependentExecutionListener.ON_TRANSACTION_COMMITTED.equals(activitiListener.getOnTransaction())) {
      executionListenerContextCloseListener.addClosedExecutionListener(executionListener, execution, executionVariablesToUse, customPropertiesMapToUse);
    } else if (TransactionDependentExecutionListener.ON_TRANSACTION_ROLLED_BACK.equals(activitiListener.getOnTransaction())) {
      executionListenerContextCloseListener.addCloseFailedExecutionListener(executionListener, execution, executionVariablesToUse, customPropertiesMapToUse);
    }
  }

}
