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

package org.activiti.engine;

import com.fasterxml.jackson.databind.node.ObjectNode;

/** Service providing access to the repository of process definitions and deployments.
 * 

 */
public interface DynamicBpmnService {

  ObjectNode getProcessDefinitionInfo(String processDefinitionId);
  
  void saveProcessDefinitionInfo(String processDefinitionId, ObjectNode infoNode);
  
  ObjectNode changeServiceTaskClassName(String id, String className);
  
  void changeServiceTaskClassName(String id, String className, ObjectNode infoNode);
  
  ObjectNode changeServiceTaskExpression(String id, String expression);
  
  void changeServiceTaskExpression(String id, String expression, ObjectNode infoNode);
  
  ObjectNode changeServiceTaskDelegateExpression(String id, String expression);
  
  void changeServiceTaskDelegateExpression(String id, String expression, ObjectNode infoNode);
  
  ObjectNode changeScriptTaskScript(String id, String script);
  
  void changeScriptTaskScript(String id, String script, ObjectNode infoNode);
  
  ObjectNode changeUserTaskName(String id, String name);
  
  void changeUserTaskName(String id, String name, ObjectNode infoNode);
  
  ObjectNode changeUserTaskDescription(String id, String description);
  
  void changeUserTaskDescription(String id, String description, ObjectNode infoNode);
  
  ObjectNode changeUserTaskDueDate(String id, String dueDate);
  
  void changeUserTaskDueDate(String id, String dueDate, ObjectNode infoNode);
  
  ObjectNode changeUserTaskPriority(String id, String priority);
  
  void changeUserTaskPriority(String id, String priority, ObjectNode infoNode);
  
  ObjectNode changeUserTaskCategory(String id, String category);
  
  void changeUserTaskCategory(String id, String category, ObjectNode infoNode);
  
  ObjectNode changeUserTaskFormKey(String id, String formKey);
  
  void changeUserTaskFormKey(String id, String formKey, ObjectNode infoNode);
  
  ObjectNode changeUserTaskAssignee(String id, String assignee);
  
  void changeUserTaskAssignee(String id, String assignee, ObjectNode infoNode);
  
  ObjectNode changeUserTaskOwner(String id, String owner);
  
  void changeUserTaskOwner(String id, String owner, ObjectNode infoNode); 
  
  ObjectNode changeUserTaskCandidateUser(String id, String candidateUser, boolean overwriteOtherChangedEntries);
  
  void changeUserTaskCandidateUser(String id, String candidateUser, boolean overwriteOtherChangedEntries, ObjectNode infoNode);

  ObjectNode changeUserTaskCandidateGroup(String id, String candidateGroup, boolean overwriteOtherChangedEntries);
  
  void changeUserTaskCandidateGroup(String id, String candidateGroup, boolean overwriteOtherChangedEntries, ObjectNode infoNode);
  
  ObjectNode changeDmnTaskDecisionTableKey(String id, String decisionTableKey);

  void changeDmnTaskDecisionTableKey(String id, String decisionTableKey, ObjectNode infoNode);
  
  ObjectNode changeSequenceFlowCondition(String id, String condition);
  
  void changeSequenceFlowCondition(String id, String condition, ObjectNode infoNode);
  
  ObjectNode getBpmnElementProperties(String id, ObjectNode infoNode);
  
  ObjectNode changeLocalizationName(String language, String id, String value);
  
  void changeLocalizationName(String language, String id, String value, ObjectNode infoNode);
  
  ObjectNode changeLocalizationDescription(String language, String id, String value);
  
  void changeLocalizationDescription(String language, String id, String value, ObjectNode infoNode);
  
  ObjectNode getLocalizationElementProperties(String language, String id, ObjectNode infoNode);
}