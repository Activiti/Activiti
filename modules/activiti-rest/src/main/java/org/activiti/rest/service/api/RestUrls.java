package org.activiti.rest.service.api;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

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

/**
 * @author Frederik Heremans
 */
public final class RestUrls {

  /**
   * Base segment for all repository-related resources: <i>repository</i>
   */
  public static final String SEGMENT_REPOSITORY_RESOURCES = "repository";
  public static final String SEGMENT_RUNTIME_RESOURCES = "runtime";
  public static final String SEGMENT_MANAGEMENT_RESOURCES = "management";
  public static final String SEGMENT_HISTORY_RESOURCES = "history";
  public static final String SEGMENT_FORM_RESOURCES = "form";
  public static final String SEGMENT_IDENTITY_RESOURCES = "identity";
  public static final String SEGMENT_QUERY_RESOURCES = "query";
  
  public static final String SEGMENT_DEPLOYMENT_RESOURCE = "deployments";
  public static final String SEGMENT_PROCESS_DEFINITION_RESOURCE = "process-definitions";
  public static final String SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE = "resources";
  public static final String SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE_CONTENT = "resourcedata";
  
  public static final String SEGMENT_TASK_RESOURCE = "tasks";
  public static final String SEGMENT_EXECUTION_RESOURCE = "executions";
  public static final String SEGMENT_PROCESS_INSTANCE_RESOURCE = "process-instances";
  public static final String SEGMENT_VARIABLES = "variables";
  public static final String SEGMENT_SUBTASKS = "subtasks";
  public static final String SEGMENT_IDENTITYLINKS = "identitylinks";
  public static final String SEGMENT_COMMENTS = "comments";
  public static final String SEGMENT_EVENTS = "events";
  public static final String SEGMENT_ATTACHMENTS = "attachments";
  public static final String SEGMENT_ATTACHMENT_CONTENT = "content";
  public static final String SEGMENT_IDENTITYLINKS_FAMILY_GROUPS = "groups";
  public static final String SEGMENT_IDENTITYLINKS_FAMILY_USERS = "users";
  public static final String SEGMENT_VARIABLE_DATA = "data";
  public static final String SEGMENT_HISTORIC_PROCESS_INSTANCE_RESOURCE = "historic-process-instances";
  public static final String SEGMENT_HISTORIC_TASK_INSTANCE_RESOURCE = "historic-task-instances";
  public static final String SEGMENT_HISTORIC_ACTIVITY_INSTANCE_RESOURCE = "historic-activity-instances";
  public static final String SEGMENT_HISTORIC_VARIABLE_INSTANCE_RESOURCE = "historic-variable-instances";
  public static final String SEGMENT_HISTORIC_DETAIL_RESOURCE = "historic-detail";
  public static final String SEGMENT_FORM_DATA = "form-data";
  public static final String SEGMENT_TABLES = "tables";
  public static final String SEGMENT_COLUMNS = "columns";
  public static final String SEGMENT_DATA = "data";
  public static final String SEGMENT_JOBS = "jobs";
  public static final String SEGMENT_JOB_EXCEPTION_STACKTRACE = "exception-stacktrace";
  public static final String SEGMENT_USERS = "users";
  public static final String SEGMENT_GROUPS = "groups";
  public static final String SEGMENT_PICTURE = "picture";
  public static final String SEGMENT_INFO = "info";
  public static final String SEGMENT_MEMBERS = "members";
  public static final String SEGMENT_MODEL = "model";
  public static final String SEGMENT_PROPERTIES = "properties";
  public static final String SEGMENT_ENGINE_INFO = "engine";
  public static final String SEGMENT_ACTIVITIES = "activities";
  public static final String SEGMENT_MODEL_RESOURCE = "models";
  public static final String SEGMENT_SOURCE = "source";
  public static final String SEGMENT_SOURCE_EXTRA = "source-extra";
  public static final String SEGMENT_DIAGRAM = "diagram";
  public static final String SEGMENT_SIGNALS = "signals";
  public static final String SEGMENT_IMAGE = "image";
  
  /**
   * URL template for the deployment collection: <i>repository/deployments</i>
   */
  public static final String[] URL_DEPLOYMENT_COLLECTION = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE};
  
  /**
   * URL template for a single deployment: <i>repository/deployments/{0:deploymentId}</i>
   */
  public static final String[] URL_DEPLOYMENT = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, "{0}"};
  
  /**
   * URL template listing deployment resources: <i>repository/deployments/{0:deploymentId}/resources</i>
   */
  public static final String[] URL_DEPLOYMENT_RESOURCES = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, 
    "{0}", SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE};
  
  /**
   * URL template for a single deployment resource: <i>repository/deployments/{0:deploymentId}/resources/{1}:resourceId</i>
   */
  public static final String[] URL_DEPLOYMENT_RESOURCE = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, 
    "{0}", SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE, "{1}"};
  
  /**
   * URL template for a single deployment resource content: <i>repository/deployments/{0:deploymentId}/resourcedata/{1}:resourceId</i>
   */
  public static final String[] URL_DEPLOYMENT_RESOURCE_CONTENT = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, 
    "{0}", SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE_CONTENT, "{1}"};
  
  /**
   * URL template for the process definition collection: <i>repository/process-definitions</i>
   */
  public static final String[] URL_PROCESS_DEFINITION_COLLECTION = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_PROCESS_DEFINITION_RESOURCE};
  
  /**
   * URL template for a single process definition: <i>repository/process-definitions/{0:processDefinitionId}</i>
   */
  public static final String[] URL_PROCESS_DEFINITION = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_PROCESS_DEFINITION_RESOURCE, "{0}"};
  
  /**
   * URL template for the resource of a single process definition: <i>repository/process-definitions/{0:processDefinitionId}/resourcedata</i>
   */
  public static final String[] URL_PROCESS_DEFINITION_RESOURCE_CONTENT = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_PROCESS_DEFINITION_RESOURCE, "{0}", SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE_CONTENT};
  
  /**
   * URL template for a process definition's identity links: <i>repository/process-definitions/{0:processDefinitionId}/identitylinks</i>
   */
  public static final String[] URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_PROCESS_DEFINITION_RESOURCE, "{0}", SEGMENT_IDENTITYLINKS};
  
  /**
   * URL template for an identitylink on a process definition: <i>repository/process-definitions/{0:processDefinitionId}/identitylinks/{1:family}/{2:identityId}</i>
   */
  public static final String[] URL_PROCESS_DEFINITION_IDENTITYLINK = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_PROCESS_DEFINITION_RESOURCE, "{0}", SEGMENT_IDENTITYLINKS, "{1}", "{2}"};
  
  /**
   * URL template for the model of a process definition: <i>repository/process-definitions/{0:processDefinitionId}/model</i>
   */
  public static final String[] URL_PROCESS_DEFINITION_MODEL = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_PROCESS_DEFINITION_RESOURCE, "{0}", SEGMENT_MODEL};
  
  /**
   * URL template for the image of a process definition: <i>repository/process-definitions/{0:processDefinitionId}/image</i>
   */
  public static final String[] URL_PROCESS_DEFINITION_IMAGE = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_PROCESS_DEFINITION_RESOURCE, "{0}", SEGMENT_IMAGE};
  
  /**
   * URL template for the model collection: <i>repository/models</i>
   */
  public static final String[] URL_MODEL_COLLECTION = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_MODEL_RESOURCE};
  
  /**
   * URL template for a single model  <i>repository/models/{0:modelId}</i>
   */
  public static final String[] URL_MODEL = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_MODEL_RESOURCE, "{0}"};
  
  /**
   * URL template for the editor source of a model  <i>repository/models/{0:modelId}/source</i>
   */
  public static final String[] URL_MODEL_SOURCE = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_MODEL_RESOURCE, "{0}", SEGMENT_SOURCE};
  
  /**
   * URL template for the extra editor source of a model  <i>repository/models/{0:modelId}/source-extra</i>
   */
  public static final String[] URL_MODEL_SOURCE_EXTRA = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_MODEL_RESOURCE, "{0}", SEGMENT_SOURCE_EXTRA};
  
  /**
   * URL template for task collection: <i>runtime/tasks</i>
   */
  public static final String[] URL_TASK_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE};
  
  /**
   * URL template for task query: <i>query/tasks</i>
   */
  public static final String[] URL_TASK_QUERY = {SEGMENT_QUERY_RESOURCES, SEGMENT_TASK_RESOURCE};
  
  /**
   * URL template for a single task: <i>runtime/tasks/{0:taskId}</i>
   */
  public static final String[] URL_TASK = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}"};
  
  /**
   * URL template for a task's sub tasks: <i>runtime/tasks/{0:taskId}/subtasks</i>
   */
  public static final String[] URL_TASK_SUBTASKS_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_SUBTASKS};
  
  /**
   * URL template for a task's variables: <i>runtime/tasks/{0:taskId}/variables</i>
   */
  public static final String[] URL_TASK_VARIABLES_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_VARIABLES};
  
  /**
   * URL template for a single task variable: <i>runtime/tasks/{0:taskId}/variables/{1:variableName}</i>
   */
  public static final String[] URL_TASK_VARIABLE = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_VARIABLES, "{1}"};
  
  /**
   * URL template for a single task variable content: <i>runtime/tasks/{0:taskId}/variables/{1:variableName}/data</i>
   */
  public static final String[] URL_TASK_VARIABLE_DATA = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_VARIABLES, "{1}", SEGMENT_VARIABLE_DATA};
  
  /**
   * URL template for a task's identity links: <i>runtime/tasks/{0:taskId}/identitylinks</i>
   */
  public static final String[] URL_TASK_IDENTITYLINKS_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_IDENTITYLINKS};
  
  /**
   * URL template for an identitylink on a task: <i>runtime/tasks/{0:taskId}/identitylinks/{1:family}/{2:identityId}/{3:type}</i>
   */
  public static final String[] URL_TASK_IDENTITYLINK = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_IDENTITYLINKS, "{1}", "{2}", "{3}"};
  
  /**
   * URL template for a task's identity links: <i>runtime/tasks/{0:taskId}/comments</i>
   */
  public static final String[] URL_TASK_COMMENT_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_COMMENTS};
  
  /**
   * URL template for a comment on a task: <i>runtime/tasks/{0:taskId}/comments/{1:commentId}</i>
   */
  public static final String[] URL_TASK_COMMENT = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_COMMENTS, "{1}"};
  
  /**
   * URL template for an task's events: <i>runtime/tasks/{0:taskId}/events</i>
   */
  public static final String[] URL_TASK_EVENT_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_EVENTS};
  
  /**
   * URL template for an event on a task: <i>runtime/tasks/{0:taskId}/events/{1:eventId}</i>
   */
  public static final String[] URL_TASK_EVENT = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_EVENTS, "{1}"};
  
  /**
   * URL template for an task's attachments: <i>runtime/tasks/{0:taskId}/attachments</i>
   */
  public static final String[] URL_TASK_ATTACHMENT_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_ATTACHMENTS};
  
  /**
   * URL template for an attachment on a task: <i>runtime/tasks/{0:taskId}/attachments/{1:attachmentId}</i>
   */
  public static final String[] URL_TASK_ATTACHMENT = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_ATTACHMENTS, "{1}"};
  
  /**
   * URL template for the content for an attachment on a task: <i>runtime/tasks/{0:taskId}/attachments/{1:attachmentId}/content</i>
   */
  public static final String[] URL_TASK_ATTACHMENT_DATA = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_TASK_RESOURCE, "{0}", SEGMENT_ATTACHMENTS, "{1}", SEGMENT_ATTACHMENT_CONTENT};
  
  /**
   * URL template for execution collection: <i>runtime/executions/{0:executionId}</i>
   */
  public static final String[] URL_EXECUTION_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_EXECUTION_RESOURCE};
  
  /**
   * URL template for a single execution: <i>runtime/executions/{0:executionId}</i>
   */
  public static final String[] URL_EXECUTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_EXECUTION_RESOURCE, "{0}"};
  
  /**
   * URL template for the variables for an execution: <i>runtime/executions/{0:executionId}/variables</i>
   */
  public static final String[] URL_EXECUTION_VARIABLE_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_EXECUTION_RESOURCE, "{0}", SEGMENT_VARIABLES};
  
  /**
   * URL template for a single variables for an execution: <i>runtime/executions/{0:executionId}/variables/{1:variableName}</i>
   */
  public static final String[] URL_EXECUTION_VARIABLE = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_EXECUTION_RESOURCE, "{0}", SEGMENT_VARIABLES, "{1}"};
  
  /**
   * URL template for a single variables for an execution: <i>runtime/executions/{0:executionId}/variables/{1:variableName}/data</i>
   */
  public static final String[] URL_EXECUTION_VARIABLE_DATA = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_EXECUTION_RESOURCE, "{0}", SEGMENT_VARIABLES, "{1}", SEGMENT_VARIABLE_DATA};
  
  /**
   * URL template for all active activities on an execution: <i>runtime/executions/{0:executionId}/activities</i>
   */
  public static final String[] URL_EXECUTION_ACTIVITIES_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_EXECUTION_RESOURCE, "{0}", SEGMENT_ACTIVITIES};
  
  /**
   * URL template for execution query: <i>query/executions</i>
   */
  public static final String[] URL_EXECUTION_QUERY = {SEGMENT_QUERY_RESOURCES, SEGMENT_EXECUTION_RESOURCE};
  
  /**
   * URL template for process instance collection: <i>runtime/process-instances</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE};
  
  /**
   * URL template for process instance query: <i>query/process-instances</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_QUERY = {SEGMENT_QUERY_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE};
  
  /**
   * URL template for a single process instance: <i>runtime/process-instances/{0:processInstanceId}</i>
   */
  public static final String[] URL_PROCESS_INSTANCE = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE, "{0}"};
  
  /**
   * URL template for the diagram for a single process instance: <i>runtime/process-instances/{0:processInstanceId}/diagram</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_DIAGRAM = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_DIAGRAM};
  
  /**
   * URL template for process instance variable collection: <i>runtime/process-instances/{0:processInstanceId}/variables</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_VARIABLE_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_VARIABLES};
  
  /**
   * URL template for a single process instance variable: <i>runtime/process-instances/{0:processInstanceId}/variables/{1:variableName}</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_VARIABLE= {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_VARIABLES, "{1}"};
  
  /**
   * URL template for a single process instance variable data: <i>runtime/process-instances/{0:processInstanceId}/variables/{1:variableName}/data</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_VARIABLE_DATA= {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_VARIABLES, "{1}", SEGMENT_VARIABLE_DATA};
  
  /**
   * URL template for a comment on a process instance: <i>history/historic-process-instances/{0:processInstanceId}/comments/{1:commentId}</i>
   */
  public static final String[] URL_HISTORIC_PROCESS_INSTANCE_COMMENT = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_COMMENTS, "{1}"};
  
  /**
   * URL template for comments on a process instance: <i>history/historic-process-instances/{0:processInstanceId}/comments</i>
   */
  public static final String[] URL_HISTORIC_PROCESS_INSTANCE_COMMENT_COLLECTION = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_COMMENTS};
  
  /**
   * URL template for a process instance's identity links: <i>runtime/process-instances/{0:processInstanceId}/identitylinks</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_IDENTITYLINKS};
  
  /**
   * URL template for an identitylink on a process instance: <i>runtime/process-instances/{0:processInstanceId}/identitylinks/users/{1:identityId}/{2:type}</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_IDENTITYLINK = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_IDENTITYLINKS, SEGMENT_IDENTITYLINKS_FAMILY_USERS, "{1}", "{2}"};
  
  /**
   * URL template for a single historic process instance: <i>history/historic-process-instances/{0:processInstanceId}</i>
   */
  public static final String[] URL_HISTORIC_PROCESS_INSTANCE = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_PROCESS_INSTANCE_RESOURCE, "{0}"};
  
  /**
   * URL template for historic process instance query: <i>history/historic-process-instances</i>
   */
  public static final String[] URL_HISTORIC_PROCESS_INSTANCES = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_PROCESS_INSTANCE_RESOURCE};
  
  /**
   * URL template for historic process instance identity links: <i>history/historic-process-instances/{0:processInstanceId}/identitylinks</i>
   */
  public static final String[] URL_HISTORIC_PROCESS_INSTANCE_IDENTITY_LINKS = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_IDENTITYLINKS};
  
  /**
   * URL template for a single historic task instance: <i>history/historic-process-instances/{0:processInstanceId}/variables/{1:variableName}</i>
   */
  public static final String[] URL_HISTORIC_PROCESS_INSTANCE_VARIABLE_DATA = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_VARIABLES, "{1}", SEGMENT_VARIABLE_DATA};
  
  /**
   * URL template for a single historic task instance: <i>history/historic-task-instances/{0:taskId}</i>
   */
  public static final String[] URL_HISTORIC_TASK_INSTANCE = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_TASK_INSTANCE_RESOURCE, "{0}"};
  
  /**
   * URL template for historic task instance query: <i>history/historic-task-instances</i>
   */
  public static final String[] URL_HISTORIC_TASK_INSTANCES = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_TASK_INSTANCE_RESOURCE};
  
  /**
   * URL template for historic task instance identity links: <i>history/historic-task-instances/{0:taskId}/identitylinks</i>
   */
  public static final String[] URL_HISTORIC_TASK_INSTANCE_IDENTITY_LINKS = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_TASK_INSTANCE_RESOURCE, "{0}", SEGMENT_IDENTITYLINKS};
  
  /**
   * URL template for a single historic task instance: <i>history/historic-task-instances/{0:taskId}/variables/{1:variableName}</i>
   */
  public static final String[] URL_HISTORIC_TASK_INSTANCE_VARIABLE_DATA = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_TASK_INSTANCE_RESOURCE, "{0}", SEGMENT_VARIABLES, "{1}", SEGMENT_VARIABLE_DATA};
  
  /**
   * URL template for historic activity instance query: <i>history/historic-activity-instances</i>
   */
  public static final String[] URL_HISTORIC_ACTIVITY_INSTANCES = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_ACTIVITY_INSTANCE_RESOURCE};
  
  /**
   * URL template for historic variable instance query: <i>history/historic-variable-instances</i>
   */
  public static final String[] URL_HISTORIC_VARIABLE_INSTANCES = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_VARIABLE_INSTANCE_RESOURCE};
  
  /**
   * URL template for a single historic variable instance data: <i>history/historic-variable-instances/{0:varInstanceId}/data</i>
   */
  public static final String[] URL_HISTORIC_VARIABLE_INSTANCE_DATA = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_VARIABLE_INSTANCE_RESOURCE, "{0}", SEGMENT_VARIABLE_DATA};
  
  /**
   * URL template for historic detail query: <i>history/historic-detail</i>
   */
  public static final String[] URL_HISTORIC_DETAIL = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_DETAIL_RESOURCE};
  
  /**
   * URL template for a single historic detail variable data: <i>history/historic-detail/{0:detailId}/data</i>
   */
  public static final String[] URL_HISTORIC_DETAIL_VARIABLE_DATA = {SEGMENT_HISTORY_RESOURCES, SEGMENT_HISTORIC_DETAIL_RESOURCE, "{0}", SEGMENT_VARIABLE_DATA};
  
  /**
   * URL template for historic process instance query: <i>query/historic-process-instances</i>
   */
  public static final String[] URL_HISTORIC_PROCESS_INSTANCE_QUERY = {SEGMENT_QUERY_RESOURCES, SEGMENT_HISTORIC_PROCESS_INSTANCE_RESOURCE};
  
  /**
   * URL template for historic process instance query: <i>query/historic-task-instances</i>
   */
  public static final String[] URL_HISTORIC_TASK_INSTANCE_QUERY = {SEGMENT_QUERY_RESOURCES, SEGMENT_HISTORIC_TASK_INSTANCE_RESOURCE};
  
  /**
   * URL template for historic activity instance query: <i>query/historic-activity-instances</i>
   */
  public static final String[] URL_HISTORIC_ACTIVITY_INSTANCE_QUERY = {SEGMENT_QUERY_RESOURCES, SEGMENT_HISTORIC_ACTIVITY_INSTANCE_RESOURCE};
  
  /**
   * URL template for historic variable instance query: <i>query/historic-variable-instances</i>
   */
  public static final String[] URL_HISTORIC_VARIABLE_INSTANCE_QUERY = {SEGMENT_QUERY_RESOURCES, SEGMENT_HISTORIC_VARIABLE_INSTANCE_RESOURCE};
  
  /**
   * URL template for historic detail query: <i>query/historic-detail</i>
   */
  public static final String[] URL_HISTORIC_DETAIL_QUERY = {SEGMENT_QUERY_RESOURCES, SEGMENT_HISTORIC_DETAIL_RESOURCE};
  
  /**
   * URL template for the collection of tables: <i>management/tables</i>
   */
  public static final String[] URL_TABLES_COLLECTION = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_TABLES};
  
  /**
   * URL template for form data: <i>form/form-data</i>
   */
  public static final String[] URL_FORM_DATA = {SEGMENT_FORM_RESOURCES, SEGMENT_FORM_DATA};
  
  /**
   * URL template for a single table: <i>management/tables/{0:tableName}</i>
   */
  public static final String[] URL_TABLE = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_TABLES, "{0}"};
  
  /**
   * URL template for column-metadata for a single table: <i>management/tables/{0:tableName}/columns</i>
   */
  public static final String[] URL_TABLE_COLUMNS = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_TABLES, "{0}", SEGMENT_COLUMNS};
  
  /**
   * URL template for actual table row data: <i>management/tables/{0:tableName}/data</i>
   */
  public static final String[] URL_TABLE_DATA = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_TABLES, "{0}", SEGMENT_DATA};
  
  /**
   * URL template for a single job: <i>management/jobs/{0:jobId}</i>
   */
  public static final String[] URL_JOB = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_JOBS, "{0}"};
  
  /**
   * URL template for the stacktrace of a single job: <i>management/jobs/{0:jobId}/exception-stacktrace</i>
   */
  public static final String[] URL_JOB_EXCEPTION_STRACKTRACE = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_JOBS, "{0}", SEGMENT_JOB_EXCEPTION_STACKTRACE};
  
  /**
   * URL template for the collection of jobs: <i>management/jobs</i>
   */
  public static final String[] URL_JOB_COLLECTION = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_JOBS};
  
  /**
   * URL template for the collection of properties: <i>management/properties</i>
   */
  public static final String[] URL_PROPERTIES_COLLECTION = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_PROPERTIES};
  
  /**
   * URL template for the collection of properties: <i>management/properties</i>
   */
  public static final String[] URL_ENGINE_INFO = {SEGMENT_MANAGEMENT_RESOURCES, SEGMENT_ENGINE_INFO};
  
  /**
   * URL template for the collection of users: <i>identity/users</i>
   */
  public static final String[] URL_USER_COLLECTION = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_USERS};
  
  /**
   * URL template for a single user: <i>identity/users/{0:userId}</i>
   */
  public static final String[] URL_USER = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_USERS, "{0}"};
  
  /**
   * URL template for the picture for a single user: <i>identity/users/{0:userId}/picture</i>
   */
  public static final String[] URL_USER_PICTURE = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_USERS, "{0}", SEGMENT_PICTURE};

  /**
   * URL template for an info entry for a single user: <i>identity/users/{0:userId}/info/{1:key}</i>
   */
  public static final String[] URL_USER_INFO = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_USERS, "{0}", SEGMENT_INFO, "{1}"};
  
  /**
   * URL template for the info collection for a single user: <i>identity/users/{0:userId}/info</i>
   */
  public static final String[] URL_USER_INFO_COLLECTION = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_USERS, "{0}", SEGMENT_INFO};
  
  /**
   * URL template for the collection of groups: <i>identity/groups</i>
   */
  public static final String[] URL_GROUP_COLLECTION = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_GROUPS};
  
  /**
   * URL template for a single group: <i>identity/groups/{0:groupId}</i>
   */
  public static final String[] URL_GROUP = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_GROUPS, "{0}"};
  
  /**
   * URL template for the membership-collection of a group: <i>identity/groups/{0:groupId}/members</i>
   */
  public static final String[] URL_GROUP_MEMBERSHIP_COLLECTION = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_GROUPS, "{0}", SEGMENT_MEMBERS};
  
  /**
   * URL template for the membership-collection of a single group membership: <i>identity/groups/{0:groupId}/members/{1:userId}</i>
   */
  public static final String[] URL_GROUP_MEMBERSHIP = {SEGMENT_IDENTITY_RESOURCES, SEGMENT_GROUPS, "{0}", SEGMENT_MEMBERS, "{1}"};
  
  
  /**
   * URL template for a signals <i>runtime/signals</i>
   */
  public static final String[] URL_SIGNALS = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_SIGNALS};
  /**
   * Creates an url based on the passed fragments and replaces any placeholders with the given arguments. The
   * placeholders are folowing the {@link MessageFormat} convention 
   * (eg. {0} is replaced by first argument value).
   */
  public static final String createRelativeResourceUrl(String[] segments, Object...arguments) {
    return MessageFormat.format(StringUtils.join(segments, '/'), arguments);
  }
}
