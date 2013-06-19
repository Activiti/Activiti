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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;



/**
 * @author Tom Baeyens
 */
public class PropertyEntity implements PersistentObject, HasRevision, Serializable {

  private static final long serialVersionUID = 1L;
  
  String name;
  int revision;
  String value;

  public PropertyEntity() {
  }

  public PropertyEntity(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  
  // persistent object methods ////////////////////////////////////////////////

  public String getId() {
    return name;
  }

  public Object getPersistentState() {
    return value;
  }

  public void setId(String id) {
    throw new ActivitiException("only provided id generation allowed for properties");
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

  // common methods  //////////////////////////////////////////////////////////

  @Override
  public String toString() {
    return "PropertyEntity[name=" + name + ", value=" + value + "]";
  }

}
