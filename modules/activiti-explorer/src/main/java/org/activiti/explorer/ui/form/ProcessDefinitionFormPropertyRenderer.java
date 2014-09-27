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
package org.activiti.explorer.ui.form;

import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.Messages;
import org.activiti.explorer.form.ProcessDefinitionFormType;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;


/**
 * @author Joram Barrez
 */
public class ProcessDefinitionFormPropertyRenderer extends AbstractFormPropertyRenderer {
  
  private static final long serialVersionUID = 1L;

  public ProcessDefinitionFormPropertyRenderer() {
    super(ProcessDefinitionFormType.class);
  }

  public Field getPropertyField(FormProperty formProperty) {
    ComboBox comboBox = new ComboBox(getPropertyLabel(formProperty));
    comboBox.setRequired(formProperty.isRequired());
    comboBox.setRequiredError(getMessage(Messages.FORM_FIELD_REQUIRED, getPropertyLabel(formProperty)));
    comboBox.setEnabled(formProperty.isWritable());
    
    List<ProcessDefinition> processDefinitions = ProcessEngines.getDefaultProcessEngine()
            .getRepositoryService()
            .createProcessDefinitionQuery()
            .orderByProcessDefinitionName().asc()
            .orderByProcessDefinitionVersion().asc()
            .list();
    
    for (ProcessDefinition processDefinition : processDefinitions) {
      comboBox.addItem(processDefinition.getId());
      String name = processDefinition.getName() + " (v" + processDefinition.getVersion() + ")";
      comboBox.setItemCaption(processDefinition.getId(), name);
    }
    
    // Select first
    if (!processDefinitions.isEmpty()) {
      comboBox.setNullSelectionAllowed(false);
      comboBox.select(processDefinitions.get(0).getId());
    }
    
    return comboBox;
  }

}
