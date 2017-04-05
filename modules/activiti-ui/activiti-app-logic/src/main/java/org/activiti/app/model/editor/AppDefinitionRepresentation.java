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
package org.activiti.app.model.editor;

import java.util.Date;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.AppDefinition;
import org.activiti.app.model.common.AbstractRepresentation;

/**
 * Pojo representation of an app definition: the metadata (name, description, etc) and the actual model ({@link AppDefinition} instance member).
 *
 */
public class AppDefinitionRepresentation extends AbstractRepresentation {

  private String id;
  private String name;
  private String key;
  private String description;
  private Integer version;
  private Date created;
  private AppDefinition definition;

  public AppDefinitionRepresentation() {
    // Empty constructor for Jackson
  }

  public AppDefinitionRepresentation(AbstractModel model) {
    this.id = model.getId();
    this.name = model.getName();
    this.key = model.getKey();
    this.description = model.getDescription();
    this.version = model.getVersion();
    this.created = model.getCreated();
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

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public AppDefinition getDefinition() {
    return definition;
  }

  public void setDefinition(AppDefinition definition) {
    this.definition = definition;
  }
}