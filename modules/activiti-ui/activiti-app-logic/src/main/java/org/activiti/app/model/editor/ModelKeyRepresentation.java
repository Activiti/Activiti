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

import org.activiti.app.model.common.AbstractRepresentation;

/**
 * Representation of model key validation
 * 
 * @author Tijs Rademakers
 */
public class ModelKeyRepresentation extends AbstractRepresentation {

  protected boolean keyAlreadyExists;
  protected String key;
  protected String id;
  protected String name;
  
  public boolean isKeyAlreadyExists() {
    return keyAlreadyExists;
  }
  
  public void setKeyAlreadyExists(boolean keyAlreadyExists) {
    this.keyAlreadyExists = keyAlreadyExists;
  }
  
  public String getKey() {
    return key;
  }
  
  public void setKey(String key) {
    this.key = key;
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
}
