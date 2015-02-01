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
package org.activiti.engine.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.activiti.engine.impl.cmd.AddIdentityLinkCmd;
import org.activiti.engine.impl.cmd.ClaimTaskCmd;
import org.activiti.engine.impl.cmd.CompleteTaskCmd;
import org.activiti.engine.impl.cmd.CreateAttachmentCmd;
import org.activiti.engine.impl.cmd.DelegateTaskCmd;
import org.activiti.engine.impl.cmd.DeleteAttachmentCmd;
import org.activiti.engine.impl.cmd.DeleteCommentCmd;
import org.activiti.engine.impl.cmd.DeleteIdentityLinkCmd;
import org.activiti.engine.impl.cmd.DeleteTaskCmd;
import org.activiti.engine.impl.cmd.GetAttachmentCmd;
import org.activiti.engine.impl.cmd.GetAttachmentContentCmd;
import org.activiti.engine.impl.cmd.GetCommentCmd;
import org.activiti.engine.impl.cmd.GetIdentityLinksForTaskCmd;
import org.activiti.engine.impl.cmd.GetProcessInstanceAttachmentsCmd;
import org.activiti.engine.impl.cmd.GetProcessInstanceCommentsCmd;
import org.activiti.engine.impl.cmd.GetSubTasksCmd;
import org.activiti.engine.impl.cmd.GetTaskAttachmentsCmd;
import org.activiti.engine.impl.cmd.GetTaskCommentsByTypeCmd;
import org.activiti.engine.impl.cmd.GetTaskCommentsCmd;
import org.activiti.engine.impl.cmd.GetTaskEventCmd;
import org.activiti.engine.impl.cmd.GetTaskEventsCmd;
import org.activiti.engine.impl.cmd.GetTaskVariableCmd;
import org.activiti.engine.impl.cmd.GetTaskVariablesCmd;
import org.activiti.engine.impl.cmd.GetTypeCommentsCmd;
import org.activiti.engine.impl.cmd.HasTaskVariableCmd;
import org.activiti.engine.impl.cmd.NewTaskCmd;
import org.activiti.engine.impl.cmd.RemoveTaskVariablesCmd;
import org.activiti.engine.impl.cmd.ResolveTaskCmd;
import org.activiti.engine.impl.cmd.SaveAttachmentCmd;
import org.activiti.engine.impl.cmd.SaveTaskCmd;
import org.activiti.engine.impl.cmd.SetTaskDueDateCmd;
import org.activiti.engine.impl.cmd.SetTaskPriorityCmd;
import org.activiti.engine.impl.cmd.SetTaskVariablesCmd;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskServiceImpl extends ServiceImpl implements TaskService {
	
	public TaskServiceImpl() {
		
	}
	
	public TaskServiceImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
		super(processEngineConfiguration);
	}

  public Task newTask() {
    return newTask(null);
  }
  
  public Task newTask(String taskId) {
    return commandExecutor.execute(new NewTaskCmd(taskId));
  }
  
  public void saveTask(Task task) {
    commandExecutor.execute(new SaveTaskCmd(task));
  }
  
  public void deleteTask(String taskId) {
    commandExecutor.execute(new DeleteTaskCmd(taskId, null, false));
  }
  
  public void deleteTasks(Collection<String> taskIds) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds, null, false));
  }
  
  public void deleteTask(String taskId, boolean cascade) {
    commandExecutor.execute(new DeleteTaskCmd(taskId, null, cascade));
  }

  public void deleteTasks(Collection<String> taskIds, boolean cascade) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds, null, cascade));
  }
  
  @Override
  public void deleteTask(String taskId, String deleteReason) {
    commandExecutor.execute(new DeleteTaskCmd(taskId, deleteReason, false));
  }
  
  @Override
  public void deleteTasks(Collection<String> taskIds, String deleteReason) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds, deleteReason, false));
  }

  public void setAssignee(String taskId, String userId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, IdentityLinkType.ASSIGNEE));
  }
  
  public void setOwner(String taskId, String userId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, IdentityLinkType.OWNER));
  }
  
  public void addCandidateUser(String taskId, String userId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, IdentityLinkType.CANDIDATE));
  }
  
  public void addCandidateGroup(String taskId, String groupId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, null, groupId, IdentityLinkType.CANDIDATE));
  }
  
  public void addUserIdentityLink(String taskId, String userId, String identityLinkType) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, identityLinkType));
  }

  public void addGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, null, groupId, identityLinkType));
  }
  
  public void deleteCandidateGroup(String taskId, String groupId) {
    commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, null, groupId, IdentityLinkType.CANDIDATE));
  }

  public void deleteCandidateUser(String taskId, String userId) {
    commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, userId, null, IdentityLinkType.CANDIDATE));
  }

  public void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
    commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, null, groupId, identityLinkType));
  }

  public void deleteUserIdentityLink(String taskId, String userId, String identityLinkType) {
    commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, userId, null, identityLinkType));
  }
  
  public List<IdentityLink> getIdentityLinksForTask(String taskId) {
    return commandExecutor.execute(new GetIdentityLinksForTaskCmd(taskId));
  }
  
  public void claim(String taskId, String userId) {
    commandExecutor.execute(new ClaimTaskCmd(taskId, userId));
  }
  
  public void unclaim(String taskId) {
    commandExecutor.execute(new ClaimTaskCmd(taskId, null));
  }

  public void complete(String taskId) {
    commandExecutor.execute(new CompleteTaskCmd(taskId, null));
  }
  
  public void complete(String taskId, Map<String, Object> variables) {
    commandExecutor.execute(new CompleteTaskCmd(taskId, variables));
  }
  
  public void complete(String taskId, Map<String, Object> variables,boolean localScope) {
  	commandExecutor.execute(new CompleteTaskCmd(taskId, variables, localScope));
  }

  public void delegateTask(String taskId, String userId) {
    commandExecutor.execute(new DelegateTaskCmd(taskId, userId));
  }

  public void resolveTask(String taskId) {
    commandExecutor.execute(new ResolveTaskCmd(taskId, null));
  }

  public void resolveTask(String taskId, Map<String, Object> variables) {
    commandExecutor.execute(new ResolveTaskCmd(taskId, variables));
  }

  public void setPriority(String taskId, int priority) {
    commandExecutor.execute(new SetTaskPriorityCmd(taskId, priority) );
  }

  public void setDueDate(String taskId, Date dueDate) {
    commandExecutor.execute(new SetTaskDueDateCmd(taskId, dueDate) );
  }
  
  public TaskQuery createTaskQuery() {
    return new TaskQueryImpl(commandExecutor, processEngineConfiguration.getDatabaseType());
  }
 
  public NativeTaskQuery createNativeTaskQuery() {
    return new NativeTaskQueryImpl(commandExecutor);
  }
  
  public Map<String, Object> getVariables(String executionId) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, null, false));
  }

  public Map<String, Object> getVariablesLocal(String executionId) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, null, true));
  }

  public Map<String, Object> getVariables(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, variableNames, false));
  }

  public Map<String, Object> getVariablesLocal(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, variableNames, true));
  }

  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetTaskVariableCmd(executionId, variableName, false));
  }

  @Override
  public <T> T getVariable(String taskId, String variableName, Class<T> variableClass) {
  	return variableClass.cast(getVariable(taskId, variableName));
  }

  public boolean hasVariable(String taskId, String variableName) {
    return commandExecutor.execute(new HasTaskVariableCmd(taskId, variableName, false));
  }
  
  public Object getVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetTaskVariableCmd(executionId, variableName, true));
  }

  @Override
  public <T> T getVariableLocal(String taskId, String variableName, Class<T> variableClass) {
  	return variableClass.cast(getVariableLocal(taskId, variableName));
  }

  public boolean hasVariableLocal(String taskId, String variableName) {
    return commandExecutor.execute(new HasTaskVariableCmd(taskId, variableName, true));
  }
  
  public void setVariable(String executionId, String variableName, Object value) {
    if(variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, false));
  }
  
  public void setVariableLocal(String executionId, String variableName, Object value) {
    if(variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, true));
  }

  public void setVariables(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, false));
  }

  public void setVariablesLocal(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, true));
  }

  public void removeVariable(String taskId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>();
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, false));
  }

  public void removeVariableLocal(String taskId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>(1);
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, true));
  }

  public void removeVariables(String taskId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, false));
  }

  public void removeVariablesLocal(String taskId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, true));
  }

  public Comment addComment(String taskId, String processInstance, String message) {
    return commandExecutor.execute(new AddCommentCmd(taskId, processInstance, message));
  }
  
  public Comment addComment(String taskId, String processInstance, String type, String message) {
    return commandExecutor.execute(new AddCommentCmd(taskId, processInstance, type, message));
  }
  
  @Override
  public Comment getComment(String commentId) {
    return commandExecutor.execute(new GetCommentCmd(commentId));
  }
  
  @Override
  public Event getEvent(String eventId) {
    return commandExecutor.execute(new GetTaskEventCmd(eventId));
  }

  public List<Comment> getTaskComments(String taskId) {
    return commandExecutor.execute(new GetTaskCommentsCmd(taskId));
  }
  
  public List<Comment> getTaskComments(String taskId, String type) {
    return commandExecutor.execute(new GetTaskCommentsByTypeCmd(taskId, type));
  }
  
  public List<Comment> getCommentsByType(String type) {
    return commandExecutor.execute(new GetTypeCommentsCmd(type));
  }

  public List<Event> getTaskEvents(String taskId) {
    return commandExecutor.execute(new GetTaskEventsCmd(taskId));
  }

  public List<Comment> getProcessInstanceComments(String processInstanceId) {
    return commandExecutor.execute(new GetProcessInstanceCommentsCmd(processInstanceId));
  }

  public List<Comment> getProcessInstanceComments(String processInstanceId, String type) {
    return commandExecutor.execute(new GetProcessInstanceCommentsCmd(processInstanceId, type));
  }

  public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content) {
    return commandExecutor.execute(new CreateAttachmentCmd(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, content, null));
  }

  public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url) {
    return commandExecutor.execute(new CreateAttachmentCmd(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, null, url));
  }

  public InputStream getAttachmentContent(String attachmentId) {
    return commandExecutor.execute(new GetAttachmentContentCmd(attachmentId));
  }

  public void deleteAttachment(String attachmentId) {
    commandExecutor.execute(new DeleteAttachmentCmd(attachmentId));
  }
  
  public void deleteComments(String taskId, String processInstanceId) {
    commandExecutor.execute(new DeleteCommentCmd(taskId, processInstanceId, null));
  }
  
  @Override
  public void deleteComment(String commentId) {
    commandExecutor.execute(new DeleteCommentCmd(null, null, commentId));
  }

  public Attachment getAttachment(String attachmentId) {
    return commandExecutor.execute(new GetAttachmentCmd(attachmentId));
  }

  public List<Attachment> getTaskAttachments(String taskId) {
    return commandExecutor.execute(new GetTaskAttachmentsCmd(taskId));
  }

  public List<Attachment> getProcessInstanceAttachments(String processInstanceId) {
    return commandExecutor.execute(new GetProcessInstanceAttachmentsCmd(processInstanceId));
  }

  public void saveAttachment(Attachment attachment) {
    commandExecutor.execute(new SaveAttachmentCmd(attachment));
  }

  public List<Task> getSubTasks(String parentTaskId) {
    return commandExecutor.execute(new GetSubTasksCmd(parentTaskId));
  }

}
