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

import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.form.Comment;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.activiti.engine.impl.cmd.GetProcessInstanceCommentsCmd;
import org.activiti.engine.impl.cmd.GetRenderedStartFormCmd;
import org.activiti.engine.impl.cmd.GetRenderedTaskFormCmd;
import org.activiti.engine.impl.cmd.GetStartFormCmd;
import org.activiti.engine.impl.cmd.GetTaskCommentsCmd;
import org.activiti.engine.impl.cmd.GetTaskFormCmd;
import org.activiti.engine.impl.cmd.SubmitStartFormCmd;
import org.activiti.engine.impl.cmd.SubmitTaskFormCmd;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public class FormServiceImpl extends ServiceImpl implements FormService {

  public Object getRenderedStartForm(String processDefinitionId) {
    return commandExecutor.execute(new GetRenderedStartFormCmd(processDefinitionId, null));
  }

  public Object getRenderedStartForm(String processDefinitionId, String engineName) {
    return commandExecutor.execute(new GetRenderedStartFormCmd(processDefinitionId, engineName));
  }

  public Object getRenderedTaskForm(String taskId) {
    return commandExecutor.execute(new GetRenderedTaskFormCmd(taskId, null));
  }

  public Object getRenderedTaskForm(String taskId, String engineName) {
    return commandExecutor.execute(new GetRenderedTaskFormCmd(taskId, engineName));
  }

  public StartFormData getStartFormData(String processDefinitionId) {
    return commandExecutor.execute(new GetStartFormCmd(processDefinitionId));
  }

  public TaskFormData getTaskFormData(String taskId) {
    return commandExecutor.execute(new GetTaskFormCmd(taskId));
  }

  public ProcessInstance submitStartFormData(String processDefinitionId, Map<String, String> properties) {
    return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, null, properties));
  }
  
  public ProcessInstance submitStartFormData(String processDefinitionId, String businessKey, Map<String, String> properties) {
	  return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, businessKey, properties));
  }

  public void submitTaskFormData(String taskId, Map<String, String> properties) {
    commandExecutor.execute(new SubmitTaskFormCmd(taskId, properties));
  }

  public void addComment(String taskId, String processInstance, String message) {
    commandExecutor.execute(new AddCommentCmd(taskId, processInstance, message));
  }

  public List<Comment> getTaskComments(String taskId) {
    return commandExecutor.execute(new GetTaskCommentsCmd(taskId));
  }

  public List<Comment> getProcessInstanceComments(String processInstanceId) {
    return commandExecutor.execute(new GetProcessInstanceCommentsCmd(processInstanceId));
  }
}
