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
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;


/**
 * @author Tijs Rademakers
 */
public class ProcessDefinitionInfoEntity implements HasRevision, PersistentObject, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String id;
  protected String processDefinitionId;
  protected int revision = 1;
  protected String infoJsonId;

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("infoJsonId", this.infoJsonId);
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public int getRevision() {
    return revision;
  }
  
  public int getRevisionNext() {
    return revision + 1;
  }
  
  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getInfoJsonId() {
    return infoJsonId;
  }

  public void setInfoJsonId(String infoJsonId) {
    this.infoJsonId = infoJsonId;
  }
}
