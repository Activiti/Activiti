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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.activiti.app.domain.common.IdBlockSize;

@Entity
@Table(name = "ACT_DE_MODEL_RELATION")
public class ModelRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "modelRelationIdGenerator")
  @TableGenerator(name = "modelRelationIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
  @Column(name = "id")
  private Long id;

  @Column(name = "parent_model_id")
  private Long parentModelId;

  // Only needed for HQL queries. Not using it!
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_model_id", insertable = false, updatable = false)
  private Model parentModel;

  @Column(name = "model_id")
  private Long modelId;

  // Only needed for HQL queries. Not using it!
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "model_id", insertable = false, updatable = false)
  private Model model;

  @Column(name = "relation_type")
  private String type;

  public ModelRelation() {

  }

  public ModelRelation(Long parentModelId, Long modelId, String type) {
    this.parentModelId = parentModelId;
    this.modelId = modelId;
    this.type = type;
  }

  public Long getParentModelId() {
    return parentModelId;
  }

  public void setParentModelId(Long parentModelId) {
    this.parentModelId = parentModelId;
  }

  public Model getParentModel() {
    return parentModel;
  }

  public void setParentModel(Model parentModel) {
    this.parentModel = parentModel;
  }

  public Long getModelId() {
    return modelId;
  }

  public void setModelId(Long modelId) {
    this.modelId = modelId;
  }

  public Model getModel() {
    return model;
  }

  public void setModel(Model model) {
    this.model = model;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
