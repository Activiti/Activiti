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

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.HasExecutionListeners;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti.engine.impl.context.Context;

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

          ExecutionListener executionListener = null;

          if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createClassDelegateExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createDelegateExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_INSTANCE.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = (ExecutionListener) activitiListener.getInstance();
          }
          
          if (executionListener != null) {
            execution.setEventName(eventType); // eventName is used to differentiate the event when reusing an execution listener for various events
            execution.setCurrentActivitiListener(activitiListener);
            executionListener.notify(execution);
            execution.setEventName(null);
            execution.setCurrentActivitiListener(null);
          }

        }
      }
    }
  }

}
