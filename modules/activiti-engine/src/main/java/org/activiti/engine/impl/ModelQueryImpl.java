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

package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;


/**
 * @author Tijs Rademakers
 */
public class ModelQueryImpl extends AbstractQuery<ModelQuery, Model> implements ModelQuery {
  
  private static final long serialVersionUID = 1L;
  protected String id;
  protected String category;
  protected String categoryLike;
  protected String categoryNotEquals;
  protected String name;
  protected String nameLike;
  protected Integer version;

  public ModelQueryImpl() {
  }

  public ModelQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public ModelQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public ModelQueryImpl modelId(String modelId) {
    this.id = modelId;
    return this;
  }
  
  public ModelQueryImpl modelCategory(String category) {
    if (category == null) {
      throw new ActivitiException("category is null");
    }
    this.category = category;
    return this;
  }
  
  public ModelQueryImpl modelCategoryLike(String categoryLike) {
    if (categoryLike == null) {
      throw new ActivitiException("categoryLike is null");
    }
    this.categoryLike = categoryLike;
    return this;
  }

  public ModelQueryImpl modelCategoryNotEquals(String categoryNotEquals) {
    if (categoryNotEquals == null) {
      throw new ActivitiException("categoryNotEquals is null");
    }
    this.categoryNotEquals = categoryNotEquals;
    return this;
  }

  public ModelQueryImpl modelName(String name) {
    if (name == null) {
      throw new ActivitiException("name is null");
    }
    this.name = name;
    return this;
  }
  
  public ModelQueryImpl modelNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }
  
  public ModelQueryImpl modelVersion(Integer version) {
    if (version == null) {
      throw new ActivitiException("version is null");
    } else if (version <= 0) {
      throw new ActivitiException("version must be positive");
    }
    this.version = version;
    return this;
  }
  
  //sorting ////////////////////////////////////////////
  
  public ModelQuery orderByModelCategory() {
    return orderBy(ModelQueryProperty.MODEL_CATEGORY);
  }
  
  public ModelQuery orderByModelId() {
    return orderBy(ModelQueryProperty.MODEL_ID);
  }
  
  public ModelQuery orderByModelVersion() {
    return orderBy(ModelQueryProperty.MODEL_VERSION);
  }
  
  public ModelQuery orderByModelName() {
    return orderBy(ModelQueryProperty.MODEL_NAME);
  }
  
  //results ////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getModelManager()
      .findModelCountByQueryCriteria(this);
  }

  public List<Model> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getModelManager()
      .findModelsByQueryCriteria(this, page);
  }
  
  //getters ////////////////////////////////////////////
  
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }
  public Integer getVersion() {
    return version;
  }
  public String getCategory() {
    return category;
  }
  public String getCategoryLike() {
    return categoryLike;
  }
  public String getCategoryNotEquals() {
    return categoryNotEquals;
  }
}
