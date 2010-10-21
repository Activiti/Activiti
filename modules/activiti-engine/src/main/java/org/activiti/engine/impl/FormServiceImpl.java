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

import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.form.StartForm;
import org.activiti.engine.form.TaskForm;
import org.activiti.engine.impl.cmd.GetRenderedStartFormCmd;
import org.activiti.engine.impl.cmd.GetRenderedTaskFormCmd;
import org.activiti.engine.impl.cmd.GetStartFormCmd;
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

  public StartForm getStartForm(String processDefinitionId) {
    return commandExecutor.execute(new GetStartFormCmd(processDefinitionId));
  }

  public TaskForm getTaskForm(String taskId) {
    return commandExecutor.execute(new GetTaskFormCmd(taskId));
  }

  public ProcessInstance submitStartForm(String processDefinitionId, Map<String, Object> properties) {
    return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, properties));
  }

  public void submitTaskForm(String taskId, Map<String, Object> properties) {
    commandExecutor.execute(new SubmitTaskFormCmd(taskId, properties));
  }

}
