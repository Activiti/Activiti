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
package org.activiti.editor.rest.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * @author Tijs Rademakers
 */
public class ModelSaveRestResource extends ServerResource implements ModelDataJsonConstants {
  
  protected static final Logger LOGGER = Logger.getLogger(ModelSaveRestResource.class.getName());

  @Put
  public void saveModel(Form modelForm) {
    ObjectMapper objectMapper = new ObjectMapper();
    String modelId = (String) getRequest().getAttributes().get("modelId");
    //System.out.println("json " + modelForm.getFirstValue("json_xml"));
    
    try {
      
      /*ObjectNode modelNode = (ObjectNode) objectMapper.readTree(modelForm.getFirstValue("json_xml"));
      JsonToBpmnExport converter = new JsonToBpmnExport(modelNode);
      byte[] bpmnBytes = converter.convert();
      System.out.println("bpmn " + new String(bpmnBytes));*/
      
      RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
      Model model = repositoryService.getModel(modelId);
      
      ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
      
      modelJson.put(MODEL_NAME, modelForm.getFirstValue("name"));
      modelJson.put(MODEL_DESCRIPTION, modelForm.getFirstValue("description"));
      model.setMetaInfo(modelJson.toString());
      model.setName(modelForm.getFirstValue("name"));
      
      repositoryService.saveModel(model);
      
      repositoryService.addModelEditorSource(model.getId(), modelForm.getFirstValue("json_xml").getBytes("utf-8"));
      repositoryService.addModelEditorSourceExtra(model.getId(), modelForm.getFirstValue("svg_xml").getBytes("utf-8"));
      
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error saving model", e);
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
  }
}
