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
package org.activiti.examples.bpmn.tasklistener;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**

 */
public class AssigneeOverwriteFromVariable implements TaskListener {

  @SuppressWarnings("unchecked")
  public void notify(DelegateTask delegateTask) {
    // get mapping table from variable
    DelegateExecution execution = delegateTask.getExecution();
    Map<String, String> assigneeMappingTable = (Map<String, String>) execution.getVariable("assigneeMappingTable");

    // get assignee from process
    String assigneeFromProcessDefinition = delegateTask.getAssignee();

    // overwrite assignee if there is an entry in the mapping table
    if (assigneeMappingTable.containsKey(assigneeFromProcessDefinition)) {
      String assigneeFromMappingTable = assigneeMappingTable.get(assigneeFromProcessDefinition);
      delegateTask.setAssignee(assigneeFromMappingTable);
    }
  }

}
