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

package org.activiti.engine;

public interface DynamicBpmnConstants {

  String BPMN_NODE = "bpmn";
  String LOCALIZATION_NODE = "localization";

  String TASK_SKIP_EXPRESSION = "taskSkipExpression";

  String SERVICE_TASK_CLASS_NAME = "serviceTaskClassName";
  String SERVICE_TASK_EXPRESSION = "serviceTaskExpression";
  String SERVICE_TASK_DELEGATE_EXPRESSION = "serviceTaskDelegateExpression";

  String SCRIPT_TASK_SCRIPT = "scriptTaskScript";

  String USER_TASK_NAME = "userTaskName";
  String USER_TASK_DESCRIPTION = "userTaskDescription";
  String USER_TASK_DUEDATE = "userTaskDueDate";
  String USER_TASK_PRIORITY = "userTaskPriority";
  String USER_TASK_CATEGORY = "userTaskCategory";
  String USER_TASK_FORM_KEY = "userTaskFormKey";
  String USER_TASK_ASSIGNEE = "userTaskAssignee";
  String USER_TASK_OWNER = "userTaskOwner";
  String USER_TASK_CANDIDATE_USERS = "userTaskCandidateUsers";
  String USER_TASK_CANDIDATE_GROUPS = "userTaskCandidateGroups";

  String DMN_TASK_DECISION_TABLE_KEY = "dmnTaskDecisionTableKey";

  String SEQUENCE_FLOW_CONDITION = "sequenceFlowCondition";

  String LOCALIZATION_LANGUAGE = "language";
  String LOCALIZATION_NAME = "name";
  String LOCALIZATION_DESCRIPTION = "description";
}
