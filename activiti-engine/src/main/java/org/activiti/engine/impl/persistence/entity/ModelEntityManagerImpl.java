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

package org.activiti.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.ModelQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.ModelDataManager;
import org.activiti.engine.repository.Model;

/**


 */
public class ModelEntityManagerImpl extends AbstractEntityManager<ModelEntity> implements ModelEntityManager {
  
  protected ModelDataManager modelDataManager;
  
  public ModelEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, ModelDataManager modelDataManager) {
    super(processEngineConfiguration);
    this.modelDataManager = modelDataManager;
  }
  
  @Override
  protected DataManager<ModelEntity> getDataManager() {
    return modelDataManager;
  }
  
  @Override
  public ModelEntity findById(String entityId) {
    return modelDataManager.findById(entityId);
  }

  @Override
  public void insert(ModelEntity model) {
    ((ModelEntity) model).setCreateTime(getClock().getCurrentTime());
    ((ModelEntity) model).setLastUpdateTime(getClock().getCurrentTime());
    
    super.insert(model);
  }

  @Override
  public void updateModel(ModelEntity updatedModel) {
    updatedModel.setLastUpdateTime(getClock().getCurrentTime());
    update(updatedModel);
  }
  
  @Override
  public void delete(String modelId) {
    ModelEntity modelEntity = findById(modelId);
    super.delete(modelEntity);
    deleteEditorSource(modelEntity);
    deleteEditorSourceExtra(modelEntity);
  }

  @Override
  public void insertEditorSourceForModel(String modelId, byte[] modelSource) {
    ModelEntity model = findById(modelId);
    if (model != null) {
      ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceValueId());
      ref.setValue("source", modelSource);

      if (model.getEditorSourceValueId() == null) {
        model.setEditorSourceValueId(ref.getId());
        updateModel(model);
      }
    }
  }

  @Override
  public void deleteEditorSource(ModelEntity model) {
    if (model.getEditorSourceValueId() != null) {
      ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceValueId());
      ref.delete();
    }
  }

  @Override
  public void deleteEditorSourceExtra(ModelEntity model) {
    if (model.getEditorSourceExtraValueId() != null) {
      ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceExtraValueId());
      ref.delete();
    }
  }

  @Override
  public void insertEditorSourceExtraForModel(String modelId, byte[] modelSource) {
    ModelEntity model = findById(modelId);
    if (model != null) {
      ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceExtraValueId());
      ref.setValue("source-extra", modelSource);

      if (model.getEditorSourceExtraValueId() == null) {
        model.setEditorSourceExtraValueId(ref.getId());
        updateModel(model);
      }
    }
  }

  @Override
  public List<Model> findModelsByQueryCriteria(ModelQueryImpl query, Page page) {
    return modelDataManager.findModelsByQueryCriteria(query, page);
  }

  @Override
  public long findModelCountByQueryCriteria(ModelQueryImpl query) {
    return modelDataManager.findModelCountByQueryCriteria(query);
  }

  @Override
  public byte[] findEditorSourceByModelId(String modelId) {
    ModelEntity model = findById(modelId);
    if (model == null || model.getEditorSourceValueId() == null) {
      return null;
    }

    ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceValueId());
    return ref.getBytes();
  }

  @Override
  public byte[] findEditorSourceExtraByModelId(String modelId) {
    ModelEntity model = findById(modelId);
    if (model == null || model.getEditorSourceExtraValueId() == null) {
      return null;
    }

    ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceExtraValueId());
    return ref.getBytes();
  }

  @Override
  public List<Model> findModelsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return modelDataManager.findModelsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findModelCountByNativeQuery(Map<String, Object> parameterMap) {
    return modelDataManager.findModelCountByNativeQuery(parameterMap);
  }

  public ModelDataManager getModelDataManager() {
    return modelDataManager;
  }

  public void setModelDataManager(ModelDataManager modelDataManager) {
    this.modelDataManager = modelDataManager;
  }
  
}
