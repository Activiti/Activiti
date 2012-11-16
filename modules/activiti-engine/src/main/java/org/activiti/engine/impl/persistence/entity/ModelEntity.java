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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.repository.Model;


/**
 * @author Tijs Rademakers
 */
public class ModelEntity implements Serializable, Model, PersistentObject {

  private static final long serialVersionUID = 1L;
  
  protected String id;
  protected String name;
  protected String category;
  protected Date createTime;
  protected Integer version;
  protected String metaInfo;
  protected String editorSourceValueId;
  protected String editorSourceExtraValueId;

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", this.name);
    persistentState.put("category", this.category);
    persistentState.put("createTime", this.createTime);
    persistentState.put("version", this.version);
    persistentState.put("metaInfo", this.metaInfo);
    persistentState.put("editorSourceValueId", this.editorSourceValueId);
    persistentState.put("editorSourceExtraValueId", this.editorSourceExtraValueId);
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getMetaInfo() {
    return metaInfo;
  }

  public void setMetaInfo(String metaInfo) {
    this.metaInfo = metaInfo;
  }

  public String getEditorSourceValueId() {
    return editorSourceValueId;
  }
  
  public void setEditorSourceValueId(String editorSourceValueId) {
    this.editorSourceValueId = editorSourceValueId;
  }

  public String getEditorSourceExtraValueId() {
    return editorSourceExtraValueId;
  }

  public void setEditorSourceExtraValueId(String editorSourceExtraValueId) {
    this.editorSourceExtraValueId = editorSourceExtraValueId;
  }
  
}
