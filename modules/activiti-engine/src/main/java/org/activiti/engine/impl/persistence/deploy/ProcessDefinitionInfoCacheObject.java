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
package org.activiti.engine.impl.persistence.deploy;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class ProcessDefinitionInfoCacheObject {
  
  protected String id;
  protected int revision;
  protected ObjectNode infoNode;
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public int getRevision() {
    return revision;
  }
  
  public void setRevision(int revision) {
    this.revision = revision;
  }
  
  public ObjectNode getInfoNode() {
    return infoNode;
  }
  
  public void setInfoNode(ObjectNode infoNode) {
    this.infoNode = infoNode;
  }
}
