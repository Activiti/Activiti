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
package org.activiti.impl.form;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.activiti.impl.task.TaskImpl;


/**
 * @author Tom Baeyens
 */
public class FormData implements Map<String, Object> {
  
  TaskImpl task;

  public FormData(TaskImpl task) {
    this.task = task;
  }

  @Override
  public void clear() {
  }

  @Override
  public boolean containsKey(Object key) {
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    return false;
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return null;
  }

  @Override
  public Object get(Object key) {
    return null;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public Set<String> keySet() {
    return null;
  }

  @Override
  public Object put(String key, Object value) {
    return null;
  }

  @Override
  public void putAll(Map< ? extends String, ? extends Object> m) {
  }

  @Override
  public Object remove(Object key) {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Collection<Object> values() {
    return null;
  }

}
