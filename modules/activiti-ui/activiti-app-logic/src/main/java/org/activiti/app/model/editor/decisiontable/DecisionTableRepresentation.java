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
package org.activiti.app.model.editor.decisiontable;

import java.util.Date;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.model.common.AbstractRepresentation;

/**
 * Created by yvoswillens on 14/08/15.
 */
public class DecisionTableRepresentation extends AbstractRepresentation {

  protected String id;
  protected String name;
  protected String key;
  protected String description;
  protected Integer version;
  protected String lastUpdatedBy;
  protected Date lastUpdated;
  protected DecisionTableDefinitionRepresentation decisionTableDefinition;

  public DecisionTableRepresentation(AbstractModel model) {
    this.id = model.getId();
    this.name = model.getName();
    this.key = model.getKey();
    this.description = model.getDescription();
    this.version = model.getVersion();
    this.lastUpdated = model.getLastUpdated();
    this.lastUpdatedBy = model.getLastUpdatedBy();
  }

  public DecisionTableRepresentation() {
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

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(String lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public DecisionTableDefinitionRepresentation getDecisionTableDefinition() {
    return decisionTableDefinition;
  }

  public void setDecisionTableDefinition(DecisionTableDefinitionRepresentation decisionTableDefinition) {
    this.decisionTableDefinition = decisionTableDefinition;
  }
}
