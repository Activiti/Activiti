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
package org.activiti.editor.data.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Tijs Rademakers
 */
@Entity
public class ModelData extends BaseDatabaseObject implements Serializable {
 
  private static final long serialVersionUID = 1L;

  protected String name;
  protected int revision;
  
  @Column(columnDefinition="TEXT")
  protected String modelJson;
  
  @Column(columnDefinition="LONGTEXT")
  protected String modelSvg;
  
  @Column(columnDefinition="LONGTEXT")
  protected String modelEditorJson;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
  public String getModelJson() {
    return modelJson;
  }
  public void setModelJson(String modelJson) {
    this.modelJson = modelJson;
  }
  public String getModelSvg() {
    return modelSvg;
  }
  public void setModelSvg(String modelSvg) {
    this.modelSvg = modelSvg;
  }
  public String getModelEditorJson() {
    return modelEditorJson;
  }
  public void setModelEditorJson(String modelEditorJson) {
    this.modelEditorJson = modelEditorJson;
  }
}
