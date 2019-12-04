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
package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.task.IdentityLink;

/**

 */
@Internal
public interface IdentityLinkEntity extends IdentityLink, Entity {

  boolean isUser();

  boolean isGroup();

  void setType(String type);

  void setUserId(String userId);

  void setGroupId(String groupId);

  void setTaskId(String taskId);

  void setProcessInstanceId(String processInstanceId);

  String getProcessDefId();

  void setProcessDefId(String processDefId);

  TaskEntity getTask();

  void setTask(TaskEntity task);

  ExecutionEntity getProcessInstance();

  void setProcessInstance(ExecutionEntity processInstance);

  ProcessDefinitionEntity getProcessDef();

  void setProcessDef(ProcessDefinitionEntity processDef);

  String getProcessDefinitionId();

}
