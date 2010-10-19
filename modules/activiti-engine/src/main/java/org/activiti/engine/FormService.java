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

import java.util.Map;

import org.activiti.engine.form.StartForm;
import org.activiti.engine.form.TaskForm;


/** Access to forms for starting new process instances and completing tasks.
 * 
 * @author Tom Baeyens
 */
public interface FormService {

  StartForm getStartForm(String processDefinitionId);
  Object getRenderedStartForm(String processDefinitionId);
  Object getRenderedStartForm(String processDefinitionId, String formEngineName);
  void submitStartForm(String processDefinitionId, Map<String, Object> properties);

  TaskForm getTaskForm(String taskId);
  Object getRenderedTaskForm(String taskId);
  Object getRenderedTaskForm(String taskId, String formEngineName);
  void submitTaskForm(String taskId, Map<String, Object> properties);
  
}
