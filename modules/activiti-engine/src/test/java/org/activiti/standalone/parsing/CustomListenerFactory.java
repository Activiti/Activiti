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
package org.activiti.standalone.parsing;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultListenerFactory;

/**
 * @author Joram Barrez
 */
public class CustomListenerFactory extends DefaultListenerFactory {
  
  @Override
  public TaskListener createExpressionTaskListener(ActivitiListener activitiListener) {
    return new TaskListener() {
      public void notify(DelegateTask delegateTask) {
        CustomListenerFactoryTest.COUNTER.addAndGet(100);
      }
    };
  }

}
