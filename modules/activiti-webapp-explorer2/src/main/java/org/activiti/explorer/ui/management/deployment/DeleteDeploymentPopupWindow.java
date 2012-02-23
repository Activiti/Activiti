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
package org.activiti.explorer.ui.management.deployment;

import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.PopupWindow;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class DeleteDeploymentPopupWindow extends PopupWindow {
  
  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  protected RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
  protected DeploymentPage deploymentPage;
  protected VerticalLayout windowLayout;
  protected Deployment deployment;
  
  public DeleteDeploymentPopupWindow(Deployment deployment, DeploymentPage deploymentPage) {
    this.deployment = deployment;
    this.deploymentPage = deploymentPage;
    this.windowLayout = (VerticalLayout) getContent();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    initWindow();
    addDeleteWarning();
    addButtons();
  }
  
  protected void initWindow() {
    windowLayout.setSpacing(true);
    addStyleName(Reindeer.WINDOW_LIGHT);
    setModal(true);
    center();
    setCaption(i18nManager.getMessage(Messages.DEPLOYMENT_DELETE_POPUP_CAPTION, deployment.getName()));
  }
  
  protected void addDeleteWarning() {
    List<ProcessDefinition> processDefinitions = 
      repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
    
    int nrOfProcessInstances = 0;
    for (ProcessDefinition processDefinition : processDefinitions) {
      nrOfProcessInstances += runtimeService.createProcessInstanceQuery()
        .processDefinitionId(processDefinition.getId())
        .count();
    }
    
    if (nrOfProcessInstances == 0) {
      Label noInstancesLabel = new Label(i18nManager.getMessage(Messages.DEPLOYMENT_NO_INSTANCES));
      noInstancesLabel.addStyleName(Reindeer.LABEL_SMALL);
      addComponent(noInstancesLabel);
    } else {
      HorizontalLayout warningLayout = new HorizontalLayout();
      warningLayout.setSpacing(true);
      addComponent(warningLayout);
      
      Embedded warningIcon = new Embedded(null, Images.WARNING);
      warningIcon.setType(Embedded.TYPE_IMAGE);
      warningLayout.addComponent(warningIcon);
      
      Label warningLabel = new Label(i18nManager.getMessage(Messages.DEPLOYMENT_DELETE_POPUP_WARNING, nrOfProcessInstances), Label.CONTENT_XHTML);
      warningLabel.setSizeUndefined();
      warningLabel.addStyleName(Reindeer.LABEL_SMALL);
      warningLayout.addComponent(warningLabel);
    }
    
    // Some empty space
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    addComponent(emptySpace);
  }
  
  protected void addButtons() {
    // Cancel
    Button cancelButton = new Button(i18nManager.getMessage(Messages.BUTTON_CANCEL));
    cancelButton.addStyleName(Reindeer.BUTTON_SMALL);
    cancelButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        close();
      }
    });
    
    // Delete
    Button deleteButton = new Button(i18nManager.getMessage(Messages.DEPLOYMENT_DELETE_POPUP_DELETE_BUTTON));
    deleteButton.addStyleName(Reindeer.BUTTON_SMALL);
    deleteButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        // Delete deployment, close popup window and refresh deployment list
        repositoryService.deleteDeployment(deployment.getId(), true);
        close();
        deploymentPage.refreshSelectNext();
      }
    });
    
    // Alignment
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setSpacing(true);
    buttonLayout.addComponent(cancelButton);
    buttonLayout.addComponent(deleteButton);
    addComponent(buttonLayout);
    windowLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
  }

}
