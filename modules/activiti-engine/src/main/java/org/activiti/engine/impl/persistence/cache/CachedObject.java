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
package org.activiti.engine.impl.persistence.cache;

import org.activiti.engine.impl.db.PersistentObject;

/**
 * @author Joram Barrez
 */
public class CachedObject {

  /**
   * The actual {@link PersistentObject} instance. 
   */
  protected PersistentObject persistentObject;
  
  /**
   * Represents the 'persistence state' at the moment this {@link CachedObject} instance was created.
   * It is used later on to determine if a {@link PersistentObject} has been updated, by comparing
   * the 'persistent state' at that moment with this instance here.
   */
  protected Object originalPersistentState;

  public CachedObject(PersistentObject persistentObject, boolean storeState) {
    this.persistentObject = persistentObject;
    if (storeState) {
      this.originalPersistentState = persistentObject.getPersistentState();
    }
  }
  
  public PersistentObject getPersistentObject() {
    return persistentObject;
  }

  public void setPersistentObject(PersistentObject persistentObject) {
    this.persistentObject = persistentObject;
  }

  public Object getOriginalPersistentState() {
    return originalPersistentState;
  }

  public void setOriginalPersistentState(Object originalPersistentState) {
    this.originalPersistentState = originalPersistentState;
  }
  
  public boolean hasChanged() {
    return persistentObject.getPersistentState() != null && !persistentObject.getPersistentState().equals(originalPersistentState);
  }

}