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
package org.activiti.cdi.impl.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;

/**
 * 
 * @author Daniel Meyer
 */
@Dependent
public class CachingBeanStore implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map<String, Object> beanStore = new HashMap<String, Object>();

	public synchronized void put(String name, Object instance) {
		beanStore.put(name, instance);
	}

	public Object getContextualInstance(String name) {
		return beanStore.get(name);
	}

	public Set<String> getVariableNames() {
		return beanStore.keySet();
	}

	public synchronized void putAll(Map<String, Object> variables) {
		beanStore.putAll(variables);
	}

	public Map<String, Object> getAll() {
		return new HashMap<String, Object>(beanStore);
	}

	public synchronized void clear() {
		beanStore.clear();
	}
	
	public boolean holdsValue(String name) {
	  return beanStore.containsKey(name);
	}

  public synchronized Map<String,Object> getAllAndClear() {
     HashMap<String, Object> hashMap = new HashMap<String, Object>(beanStore);
     beanStore.clear();
     return hashMap;
  }

}
