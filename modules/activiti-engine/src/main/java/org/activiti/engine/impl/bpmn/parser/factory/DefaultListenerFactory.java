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
package org.activiti.engine.impl.bpmn.parser.factory;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionExecutionListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionTaskListener;
import org.activiti.engine.impl.bpmn.listener.ExpressionExecutionListener;
import org.activiti.engine.impl.bpmn.listener.ExpressionTaskListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * Default implementation of the {@link ListenerFactory}. 
 * Used when no custom {@link ListenerFactory} is injected on 
 * the {@link ProcessEngineConfigurationImpl}.
 * 
 * @author Joram Barrez
 */
public class DefaultListenerFactory extends AbstractBehaviorFactory implements ListenerFactory {
  
  public TaskListener createClassDelegateTaskListener(ActivitiListener activitiListener) {
    return new ClassDelegate(activitiListener.getImplementation(), createFieldDeclarations(activitiListener.getFieldExtensions()));
  }
  
  public TaskListener createExpressionTaskListener(ActivitiListener activitiListener) {
    return new ExpressionTaskListener(expressionManager.createExpression(activitiListener.getImplementation()));
  }
  
  public TaskListener createDelegateExpressionTaskListener(ActivitiListener activitiListener) {
    return new DelegateExpressionTaskListener(expressionManager.createExpression(activitiListener.getImplementation()), 
            createFieldDeclarations(activitiListener.getFieldExtensions()));
  }

  public ExecutionListener createClassDelegateExecutionListener(ActivitiListener activitiListener) {
    return new ClassDelegate(activitiListener.getImplementation(), createFieldDeclarations(activitiListener.getFieldExtensions()));
  }
  
  public ExecutionListener createExpressionExecutionListener(ActivitiListener activitiListener) {
    return new ExpressionExecutionListener(expressionManager.createExpression(activitiListener.getImplementation()));
  }
  
  public ExecutionListener createDelegateExpressionExecutionListener(ActivitiListener activitiListener) {
    return new DelegateExpressionExecutionListener(expressionManager.createExpression(activitiListener.getImplementation()), 
            createFieldDeclarations(activitiListener.getFieldExtensions()));
  }
  
}
