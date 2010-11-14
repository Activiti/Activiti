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

package org.activiti.engine.impl.history.handler;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.pvm.delegate.TaskListener;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.task.TaskEntity;


/**
 * @author Tom Baeyens
 */
public class UserTaskAssignmentHandler implements TaskListener {

  public void notify(DelegateTask task) {
    ExecutionEntity execution = ((TaskEntity) task).getExecution();
    if (execution != null) {
      HistoricActivityInstanceEntity historicActivityInstance = ActivityInstanceEndHandler.findActivityInstance(execution);
      historicActivityInstance.setAssignee(task.getAssignee());
    }
  }
  
}
