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
package org.activiti.explorer.ui.process;

import java.text.MessageFormat;

import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.form.FormPropertiesEventListener;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.form.FormPropertiesForm.FormPropertiesEvent;
import org.activiti.explorer.ui.process.listener.ConvertProcessDefinitionToModelClickListener;
import org.activiti.explorer.ui.process.listener.StartProcessInstanceClickListener;

import com.vaadin.ui.Button;


/**
 * Panel showing process definition detail.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class ProcessDefinitionDetailPanel extends AbstractProcessDefinitionDetailPanel {
  
  private static final long serialVersionUID = 1L;
  
  protected Button startProcessInstanceButton;
  protected Button editProcessDefinitionButton;
  protected FormPropertiesForm processDefinitionStartForm;
  
  public ProcessDefinitionDetailPanel(String processDefinitionId, ProcessDefinitionPage processDefinitionPage) {
    super(processDefinitionId, processDefinitionPage);
  }
  
  protected void initActions(AbstractPage parentPage) {
    ProcessDefinitionPage processDefinitionPage = (ProcessDefinitionPage) parentPage;

    startProcessInstanceButton = new Button(i18nManager.getMessage(Messages.PROCESS_START));
    startProcessInstanceButton.addListener(new StartProcessInstanceClickListener(processDefinition, processDefinitionPage));
    
    editProcessDefinitionButton = new Button(i18nManager.getMessage(Messages.PROCESS_CONVERT));
    editProcessDefinitionButton.addListener(new ConvertProcessDefinitionToModelClickListener(processDefinition));
    
    if(((ProcessDefinitionEntity) processDefinition).isGraphicalNotationDefined() == false) {
      editProcessDefinitionButton.setEnabled(false);
    }
    
    // Clear toolbar and add 'start' button
    processDefinitionPage.getToolBar().removeAllButtons();
    processDefinitionPage.getToolBar().addButton(startProcessInstanceButton);
    processDefinitionPage.getToolBar().addButton(editProcessDefinitionButton);
  }
  
  public void showProcessStartForm(StartFormData startFormData) {
    if(processDefinitionStartForm == null) {
      processDefinitionStartForm = new FormPropertiesForm();
      processDefinitionStartForm.setSubmitButtonCaption(i18nManager.getMessage(Messages.PROCESS_START));
      processDefinitionStartForm.setCancelButtonCaption(i18nManager.getMessage(Messages.BUTTON_CANCEL));
      
      // When form is submitted/cancelled, show the info again
      processDefinitionStartForm.addListener(new FormPropertiesEventListener() {
        private static final long serialVersionUID = 1L;
        protected void handleFormSubmit(FormPropertiesEvent event) {
          formService.submitStartFormData(processDefinition.getId(), event.getFormProperties());
          
          // Show notification
          ExplorerApp.get().getMainWindow().showNotification(MessageFormat.format(
            i18nManager.getMessage(Messages.PROCESS_STARTED_NOTIFICATION), getProcessDisplayName(processDefinition)));
          initProcessDefinitionInfo();
        }
        protected void handleFormCancel(FormPropertiesEvent event) {
          initProcessDefinitionInfo();
        }
      });
    }
    processDefinitionStartForm.setFormProperties(startFormData.getFormProperties());
    
    startProcessInstanceButton.setEnabled(false);
    detailContainer.removeAllComponents();
    detailContainer.addComponent(processDefinitionStartForm);
  }
  
  @Override
  public void initProcessDefinitionInfo() {
    super.initProcessDefinitionInfo();
    
    if (startProcessInstanceButton != null) {
      startProcessInstanceButton.setEnabled(true);
    }
  }
  
}
