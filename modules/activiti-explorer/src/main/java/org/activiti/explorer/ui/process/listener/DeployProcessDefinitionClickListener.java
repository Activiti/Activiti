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

package org.activiti.explorer.ui.process.listener;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Tijs Rademakers
 */
public class DeployProcessDefinitionClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  protected RepositoryService repositoryService;
  protected NotificationManager notificationManager;
  
  protected Model modelData;
  
  public DeployProcessDefinitionClickListener(Model model) {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.notificationManager = ExplorerApp.get().getNotificationManager(); 
    
    this.modelData = model;
  }

  public void buttonClick(ClickEvent event) {
    
    try {
      ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
      BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
      byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
      
      Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
          .addString(modelData.getName() + ".bpmn20.xml", new String(bpmnBytes)).deploy();
      
      ExplorerApp.get().getViewManager().showDeploymentPage(deployment.getId());
      
    } catch(Exception e) {
      notificationManager.showErrorNotification(Messages.PROCESS_TOXML_FAILED, e);
    }
  }
}
