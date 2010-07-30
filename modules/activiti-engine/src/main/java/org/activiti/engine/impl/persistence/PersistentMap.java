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
package org.activiti.engine.impl.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Tom Baeyens
 */
public class PersistentMap<T,U> extends HashMap<T, U> {

  private static final long serialVersionUID = 1L;
  
  protected List<U> added = new ArrayList<U>();
  protected List<U> removed = new ArrayList<U>();

  public U put(T key, U value) {
    added.add(value);
    U removedValue = super.put(key, value);
    if (removedValue!=null) {
      removed.add(removedValue);
    }
    return removedValue;
  }

  public U remove(Object key) {
    U removedValue = super.remove(key);
    if (removedValue!=null) {
      removed.add(removedValue);
    }
    return removedValue;
  }

  public void clear() {
    throw new UnsupportedOperationException("this method is not available on "+getClass().getName());
  }
}
