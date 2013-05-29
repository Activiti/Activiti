package org.activiti.rest.api;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

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
  public static final String SEGMENT_QUERY_RESOURCES = "query";
  
  public static final String SEGMENT_DEPLOYMENT_RESOURCE = "deployments";
  public static final String SEGMENT_PROCESS_DEFINITION_RESOURCE = "process-definitions";
  public static final String SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE = "resources";
  public static final String SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE_CONTENT = "resourcedata";
  
  public static final String SEGMENT_TASK_RESOURCE = "tasks";
  public static final String SEGMENT_EXECUTION_RESOURCE = "executions";
  public static final String SEGMENT_PROCESS_INSTANCE_RESOURCE = "process-instances";
  public static final String SEGMENT_VARIABLES = "variables";
  public static final String SEGMENT_IDENTITYLINKS = "identitylinks";
  public static final String SEGMENT_COMMENTS = "comments";
  public static final String SEGMENT_EVENTS = "events";
  public static final String SEGMENT_ATTACHMENTS = "attachments";
  public static final String SEGMENT_ATTACHMENT_CONTENT = "content";
  public static final String SEGMENT_IDENTITYLINKS_FAMILY_GROUPS = "groups";
  public static final String SEGMENT_IDENTITYLINKS_FAMILY_USERS = "users";
  public static final String SEGMENT_VARIABLE_DATA = "data";
  
  /**
   * URL template for the deployment collection: <i>repository/deployments</i>
   */
  public static final String[] URL_DEPLOYMENT_COLLECTION = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE};
  
  /**
   * URL template for a single deployment: <i>repository/deployments/{0:deploymentId}</i>
   */
  public static final String[] URL_DEPLOYMENT = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, "{0}"};
  
  /**
   * URL template listing deployment resources: <i>repository/deployments/{0:deploymentId}/resources/{1}:resourceId</i>
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
   * URL template for task collection: <i>runtime/tasks/{0:taskId}</i>
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
   * URL template for a comment on a process instance: <i>runtime/process-instances/{0:processInstanceId}/comments/{1:commentId}</i>
   */
  public static final String[] URL_PROCESS_INSTANCE_COMMENT = {SEGMENT_RUNTIME_RESOURCES, SEGMENT_PROCESS_INSTANCE_RESOURCE, "{0}", SEGMENT_COMMENTS, "{1}"};
  
  
  /**
   * Creates an url based on the passed fragments and replaces any placeholders with the given arguments. The
   * placeholders are folowing the {@link MessageFormat} convention 
   * (eg. {0} is replaced by first argument value).
   */
  public static final String createRelativeResourceUrl(String[] segments, Object...arguments) {
    return MessageFormat.format(StringUtils.join(segments, '/'), arguments);
  }
}
