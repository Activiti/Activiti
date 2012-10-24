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

package org.activiti.editor.data.dao;

import java.util.List;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.data.model.ModelData;
import org.activiti.editor.exception.ModelException;
import org.codehaus.jackson.JsonNode;

/**
 * @author Tijs Rademakers
 */
public class ModelDao extends BaseDao implements ModelDataJsonConstants {

  public ModelData getModelById(long id) {
    return getObjectById(ModelData.class, id);
  }
  
  public List<ModelData> getModelByName(String modelName) {
    return getQueryResult("FROM ModelData WHERE name like :name", "name", modelName, ModelData.class);
  }
  
  public List<ModelData> getModels() {
    return getQueryResult("FROM ModelData", null, ModelData.class);
  }
  
  public long saveModel(ModelData model) {
    try {
      JsonNode modelNode = objectMapper.readTree(model.getModelJson());
      model.setName(modelNode.get(MODEL_NAME).getTextValue());
      model.setRevision(modelNode.get(MODEL_REVISION).getNumberValue().intValue());
    } catch(Exception e) {
      throw new ModelException("Model Json tree could not be read");
    }
    return saveObject(model);
  }
  
  public void deleteModel(ModelData model) {
    deleteObject(model);
  }
}
