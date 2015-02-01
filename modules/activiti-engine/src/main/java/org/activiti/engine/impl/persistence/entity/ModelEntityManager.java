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

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.ModelQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;


/**
 * @author Tijs Rademakers
 */
public class ModelEntityManager extends AbstractManager {

  public Model createNewModel() {
    return new ModelEntity();
  }

  public void insertModel(Model model) {
    ((ModelEntity) model).setCreateTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
    ((ModelEntity) model).setLastUpdateTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
    getDbSqlSession().insert((PersistentObject) model);
    
    if(Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, model));
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, model));
    }
  }

  public void updateModel(ModelEntity updatedModel) {
    CommandContext commandContext = Context.getCommandContext();
    updatedModel.setLastUpdateTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.update(updatedModel);
    
    if(Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, updatedModel));
    }
  }

  public void deleteModel(String modelId) {
    ModelEntity model = getDbSqlSession().selectById(ModelEntity.class, modelId);
    getDbSqlSession().delete(model);
    deleteEditorSource(model);
    deleteEditorSourceExtra(model);
    
    if(Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, model));
    }
  }
  
  public void insertEditorSourceForModel(String modelId, byte[] modelSource) {
    ModelEntity model = findModelById(modelId);
    if (model != null) {
      ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceValueId());
      ref.setValue("source", modelSource);
      
      if (model.getEditorSourceValueId() == null) {
        model.setEditorSourceValueId(ref.getId());
        updateModel(model);
      }
    }
  }
  
  public void deleteEditorSource(ModelEntity model) {
    if (model.getEditorSourceValueId() != null) {
      ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceValueId());
      ref.delete();
    }
  }
  
  public void deleteEditorSourceExtra(ModelEntity model) {
    if (model.getEditorSourceExtraValueId() != null) {
      ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceExtraValueId());
      ref.delete();
    }
  }
  
  public void insertEditorSourceExtraForModel(String modelId, byte[] modelSource) {
    ModelEntity model = findModelById(modelId);
    if (model != null) {
      ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceExtraValueId());
      ref.setValue("source-extra", modelSource);
      
      if (model.getEditorSourceExtraValueId() == null) {
        model.setEditorSourceExtraValueId(ref.getId());
        updateModel(model);
      }
    }
  }

  public ModelQuery createNewModelQuery() {
    return new ModelQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutor());
  }

  @SuppressWarnings("unchecked")
  public List<Model> findModelsByQueryCriteria(ModelQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectModelsByQueryCriteria", query, page);
  }
  
  public long findModelCountByQueryCriteria(ModelQueryImpl query) {
    return (Long) getDbSqlSession().selectOne("selectModelCountByQueryCriteria", query);
  }

  public ModelEntity findModelById(String modelId) {
    return (ModelEntity) getDbSqlSession().selectOne("selectModel", modelId);
  }
  
  public byte[] findEditorSourceByModelId(String modelId) {
    ModelEntity model = findModelById(modelId);
    if (model == null || model.getEditorSourceValueId() == null) {
      return null;
    }
    
    ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceValueId());
    return ref.getBytes();
  }
  
  public byte[] findEditorSourceExtraByModelId(String modelId) {
    ModelEntity model = findModelById(modelId);
    if (model == null || model.getEditorSourceExtraValueId() == null) {
      return null;
    }
    
    ByteArrayRef ref = new ByteArrayRef(model.getEditorSourceExtraValueId());
    return ref.getBytes();
  }

  @SuppressWarnings("unchecked")
  public List<Model> findModelsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectModelByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findModelCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectModelCountByNativeQuery", parameterMap);
  }
}
