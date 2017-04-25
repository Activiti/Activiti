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
package org.activiti.app.domain.editor;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.activiti.app.domain.common.IdBlockSize;
import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public class AbstractModel {

  public static final int MODEL_TYPE_BPMN = 0;
  public static final int MODEL_TYPE_FORM = 2;
  public static final int MODEL_TYPE_APP = 3;
  public static final int MODEL_TYPE_DECISION_TABLE = 4;


  @Id
  @GeneratedValue(generator = "modelIdGenerator")
  @GenericGenerator(name = "modelIdGenerator", strategy = "uuid2")
  @Column(name = "id", unique = true)
  protected String id;

  @Column(name = "name")
  protected String name;
  
  @Column(name = "model_key")
  protected String key;

  @Column(name = "description")
  protected String description;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created")
  protected Date created;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_updated")
  protected Date lastUpdated;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "last_updated_by")
  private String lastUpdatedBy;

  @Column(name = "version")
  protected int version;

  @Column(name = "model_editor_json")
  protected String modelEditorJson;

  @Column(name = "model_comment")
  protected String comment;

  @Column(name = "model_type")
  protected Integer modelType;

  public AbstractModel() {
    this.created = new Date();
  }

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

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(String lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getModelEditorJson() {
    return modelEditorJson;
  }

  public void setModelEditorJson(String modelEditorJson) {
    this.modelEditorJson = modelEditorJson;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getComment() {
    return comment;
  }

  public Integer getModelType() {
    return modelType;
  }

  public void setModelType(Integer modelType) {
    this.modelType = modelType;
  }
}