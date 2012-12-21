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

import java.util.Date;
import java.util.List;

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
    ((ModelEntity) model).setCreateTime(new Date());
    getDbSqlSession().insert((PersistentObject) model);
  }

  public void updateModel(ModelEntity updatedModel) {
    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.update(updatedModel);
  }

  public void deleteModel(String modelId) {
    ModelEntity model = getDbSqlSession().selectById(ModelEntity.class, modelId);
    getDbSqlSession().delete(model);
    deleteEditorSource(model);
    deleteEditorSourceExtra(model);
  }
  
  public void insertEditorSourceForModel(String modelId, byte[] modelSource) {
    ModelEntity model = findModelById(modelId);
    if (model != null) {
      ByteArrayEntity byteArrayValue = null;
      if (model.getEditorSourceValueId() != null) {
        Context
          .getCommandContext()
          .getByteArrayEntityManager()
          .deleteByteArrayById(model.getEditorSourceValueId());
      }
      if (modelSource != null) {
        byteArrayValue = new ByteArrayEntity(modelSource);
        Context
          .getCommandContext()
          .getDbSqlSession()
          .insert(byteArrayValue);
      }
      
      if (byteArrayValue != null) {
        model.setEditorSourceValueId(byteArrayValue.getId());
        updateModel(model);
      }
    }
  }
  
  public void deleteEditorSource(ModelEntity model) {
    if (model.getEditorSourceValueId() != null) {
      ByteArrayEntity data = getDbSqlSession().selectById(ByteArrayEntity.class, model.getEditorSourceValueId());
      getDbSqlSession().delete(data);
    }
  }
  
  public void deleteEditorSourceExtra(ModelEntity model) {
    if (model.getEditorSourceExtraValueId() != null) {
      ByteArrayEntity data = getDbSqlSession().selectById(ByteArrayEntity.class, model.getEditorSourceExtraValueId());
      getDbSqlSession().delete(data);
    }
  }
  
  public void insertEditorSourceExtraForModel(String modelId, byte[] modelSource) {
    ModelEntity model = findModelById(modelId);
    if (model != null) {
      ByteArrayEntity byteArrayValue = null;
      if (model.getEditorSourceExtraValueId() != null) {
        Context
          .getCommandContext()
          .getByteArrayEntityManager()
          .deleteByteArrayById(model.getEditorSourceExtraValueId());
      }
      if (modelSource != null) {
        byteArrayValue = new ByteArrayEntity(modelSource);
        Context
          .getCommandContext()
          .getDbSqlSession()
          .insert(byteArrayValue);
      }
      
      if (byteArrayValue != null) {
        model.setEditorSourceExtraValueId(byteArrayValue.getId());
        updateModel(model);
      }
    }
  }

  public ModelQuery createNewModelQuery() {
    return new ModelQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
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
    byte[] bytes = null;
    ModelEntity model = findModelById(modelId);
    if (model != null && model.getEditorSourceValueId() != null) {
      ByteArrayEntity byteEntity = Context
          .getCommandContext()
          .getDbSqlSession()
          .selectById(ByteArrayEntity.class, model.getEditorSourceValueId());
      if (byteEntity != null) {
        bytes = byteEntity.getBytes();
      }
    }
    return bytes;
  }
  
  public byte[] findEditorSourceExtraByModelId(String modelId) {
    byte[] bytes = null;
    ModelEntity model = findModelById(modelId);
    if (model != null && model.getEditorSourceExtraValueId() != null) {
      ByteArrayEntity byteEntity = Context
          .getCommandContext()
          .getDbSqlSession()
          .selectById(ByteArrayEntity.class, model.getEditorSourceExtraValueId());
      if (byteEntity != null) {
        bytes = byteEntity.getBytes();
      }
    }
    return bytes;
  }
}
