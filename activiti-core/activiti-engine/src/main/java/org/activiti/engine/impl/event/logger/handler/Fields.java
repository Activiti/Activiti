/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.event.logger.handler;


public interface Fields {

  String ACTIVITY_ID = "activityId";
  String ACTIVITY_NAME = "activityName";
  String ACTIVITY_TYPE = "activityType";
  String ASSIGNEE = "assignee";
  String BEHAVIOR_CLASS = "behaviorClass";
  String BUSINESS_KEY = "businessKey";
  String CATEGORY = "category";
  String CREATE_TIME = "createTime";
  String DESCRIPTION = "description";
  String DUE_DATE = "dueDate";
  String DURATION = "duration";
  String ERROR_CODE = "errorCode";
  String END_TIME = "endTime";
  String EXECUTION_ID = "executionId";
  String FORM_KEY = "formKey";
  String ID = "id";
  String MESSAGE_NAME = "messageName";
  String MESSAGE_DATA = "messageData";
  String NAME = "name";
  String OWNER = "owner";
  String PRIORITY = "priority";
  String PROCESS_DEFINITION_ID = "processDefinitionId";
  String TASK_DEFINITION_KEY = "taskDefinitionKey";
  String PROCESS_INSTANCE_ID = "processInstanceId";
  String PROCESS_INSTANCE_NAME = "processInstanceName";
  String SIGNAL_NAME = "signalName";
  String SIGNAL_DATA = "signalData";
  String SOURCE_ACTIVITY_ID = "sourceActivityId";
  String SOURCE_ACTIVITY_NAME = "sourceActivityName";
  String SOURCE_ACTIVITY_TYPE = "sourceActivityType";
  String SOURCE_ACTIVITY_BEHAVIOR_CLASS = "sourceActivityBehaviorClass";
  String TARGET_ACTIVITY_ID = "targetActivityId";
  String TARGET_ACTIVITY_NAME = "targetActivityName";
  String TARGET_ACTIVITY_TYPE = "targetActivityType";
  String TARGET_ACTIVITY_BEHAVIOR_CLASS = "targetActivityBehaviorClass";
  String TENANT_ID = "tenantId";
  String TIMESTAMP = "timeStamp";
  String USER_ID = "userId";
  String LOCAL_VARIABLES = "localVariables";
  String VARIABLES = "variables";
  String VALUE = "value";
  String VALUE_BOOLEAN = "booleanValue";
  String VALUE_DATE = "dateValue";
  String VALUE_DOUBLE = "doubleValue";
  String VALUE_INTEGER = "integerValue";
  String VALUE_JSON = "jsonValue";
  String VALUE_LONG = "longValue";
  String VALUE_SHORT = "shortValue";
  String VALUE_STRING = "stringValue";
  String VALUE_UUID = "uuidValue";
  String VARIABLE_TYPE = "variableType";

}
