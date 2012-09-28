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

import org.activiti.editor.data.dao.ModelDao;
import org.activiti.editor.data.model.ModelData;
import org.activiti.editor.json.constants.ModelDataJsonConstants;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * @author Tijs Rademakers
 */
public class ModelEditorJsonRestResource extends ServerResource implements ModelDataJsonConstants {
  
  protected static final Logger LOGGER = Logger.getLogger(ModelEditorJsonRestResource.class.getName());
  private ObjectMapper objectMapper = new ObjectMapper();
  
  @Get
  public ObjectNode getEditorJson() {
    ObjectNode modelNode = null;
    String modelId = (String) getRequest().getAttributes().get("modelId");
    
    if(NumberUtils.isNumber(modelId)) {
      ModelDao modelDao = new ModelDao();
      ModelData model = modelDao.getModelById(Long.valueOf(modelId));
      
      if (model != null) {
        try {
          modelNode = (ObjectNode) objectMapper.readTree(model.getModelJson());
          modelNode.put(MODEL_ID, model.getObjectId());
          ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
          modelNode.put("model", editorJsonNode);
          
        } catch(Exception e) {
          LOGGER.log(Level.SEVERE, "Error creating model JSON", e);
          setStatus(Status.SERVER_ERROR_INTERNAL);
        }
      }
    }
    return modelNode;
  }
}
