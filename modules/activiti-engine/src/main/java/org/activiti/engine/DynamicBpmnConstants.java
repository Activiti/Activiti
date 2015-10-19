package org.activiti.engine;

public interface DynamicBpmnConstants {

  String BPMN_NODE = "bpmn";
  String LOCALIZATION_NODE = "localization";
  
  String TASK_SKIP_EXPRESSION = "taskSkipExpression";
  
  String SERVICE_TASK_CLASS_NAME = "serviceTaskClassName";
  String SERVICE_TASK_EXPRESSION = "serviceTaskExpression";
  String SERVICE_TASK_DELEGATE_EXPRESSION = "serviceTaskDelegateExpression";
  
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

  String LOCALIZATION_LANGUAGE = "language";
  String LOCALIZATION_NAME = "name";
  String LOCALIZATION_DESCRIPTION = "description";
}
