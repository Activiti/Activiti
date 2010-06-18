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
package org.activiti.impl.tx;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.ActivitiException;
import org.activiti.impl.interceptor.CommandContext;



/**
 * @author Tom Baeyens
 */
public class TransactionalObjectFactory {

  Map<Class<?>, Descriptor> descriptors;
  Map<Class<?>, Object> cache;
  
  public TransactionalObjectFactory(List<Descriptor> descriptors) {
    this.descriptors = new HashMap<Class<?>, Descriptor>();
    this.cache = new HashMap<Class<?>, Object>();
    for (Descriptor descriptor: descriptors) {
      addDescriptor(descriptor);
    }
  }

  public <T> T get(Class<T> type, CommandContext transactionalContext) {
    T object = (T) cache.get(type);
    if (object==null) {
      Descriptor descriptor = descriptors.get(type);
      if (descriptor==null) {
        throw new ActivitiException("no transactional object descriptor for '"+type.getName()+"'");
      }
      object = (T) descriptor.getObject(transactionalContext, this);
      cache.put(type, object);
    }
    return object;
  }
  
  public void addDescriptor(Descriptor descriptor) {
    addDescriptor(descriptor.getType(), descriptor);
  }
  public void addDescriptor(Class<?> type, Descriptor descriptor) {
    this.descriptors.put(type, descriptor);
    if (type.getSuperclass()!=null) {
      addDescriptor(type.getSuperclass(), descriptor);
    }
    for (Class<?> interfaceType: type.getInterfaces()) {
      addDescriptor(interfaceType, descriptor);
    }
  }
  
  public Collection<Session> getInstantiatedSessions() {
    return (Collection) cache.values();
  }

  public void resetCache() {
    this.cache = new HashMap<Class<?>, Object>();
  }
}
