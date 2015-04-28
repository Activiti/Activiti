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
package org.activiti.explorer.ui.process.simple.editor.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.explorer.ui.process.simple.editor.listener.TaskFormModelListener;
import org.activiti.workflow.simple.definition.form.FormDefinition;

/**
 * @author Joram Barrez
 */
public class TaskFormModel implements Serializable {

  protected List<TaskFormModelListener> formModelListeners = new ArrayList<TaskFormModelListener>();

  /* Mapping id of task in taskTable <-> FormDefinition */
  protected Map<Object, FormDefinition> forms = new HashMap<Object, FormDefinition>();

  public void addForm(Object taskItemId, FormDefinition form) {
    forms.put(taskItemId, form);
    fireFormAdded(taskItemId);
  }

  public void removeForm(Object taskItemId) {
    forms.remove(taskItemId);
    fireFormRemoved(taskItemId);
  }

  public FormDefinition getForm(Object id) {
    return forms.get(id);
  }

  public void addFormModelListener(TaskFormModelListener formAdditionListener) {
    formModelListeners.add(formAdditionListener);
  }

  protected void fireFormAdded(Object taskItemId) {
    for (TaskFormModelListener formAdditionListener : formModelListeners) {
      formAdditionListener.formAdded(taskItemId);
    }
  }

  protected void fireFormRemoved(Object taskItemId) {
    for (TaskFormModelListener formModelListener : formModelListeners) {
      formModelListener.formRemoved(taskItemId);
    }
  }

}
