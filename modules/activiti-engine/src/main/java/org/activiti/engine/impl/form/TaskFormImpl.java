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

package org.activiti.engine.impl.form;

import org.activiti.engine.form.TaskForm;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class TaskFormImpl extends FormImpl implements TaskForm {

  private static final long serialVersionUID = 1L;
  
  protected Task task;
  
  public TaskFormImpl(String formKey, String deploymentId, TaskEntity task) {
    super(formKey, deploymentId);
    this.task = task;
  }

  public Task getTask() {
    return task;
  }
}
