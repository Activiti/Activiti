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
package org.activiti.impl.execution;

import java.io.Serializable;

import org.activiti.impl.Cmd;
import org.activiti.impl.persistence.PersistentObject;


/**
 * Very rough stub of a Job
 * @author Tom Baeyens
 */
public abstract class JobImpl<T> implements Serializable, PersistentObject, Cmd <T> {
  private static final long serialVersionUID = 1L;
  private long id;

  public String getId() {
    return Long.toString(id);
  }
  public long getIdL() {
    return id;
  }

  public Object getPersistentState() {
    // TODO Implement
    return null;
  }

  public void setId(String id) {
    this.id = Long.parseLong(id);
  }
  public void setId(long id) {
    this.id = id;
  }
}
