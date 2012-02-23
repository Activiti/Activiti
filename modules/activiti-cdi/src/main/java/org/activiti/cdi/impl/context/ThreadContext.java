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

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 * Context for managing thread-scoped state. 
 * 
 * We need this for managing thread-scoped business process associations, see {@link DefaultBusinessProcessAssociationManager}
 *
 * @author Daniel Meyer
 */
public class ThreadContext implements Context {
  
  private ThreadLocal<CachingBeanStore> beanStore = new ThreadLocal<CachingBeanStore>();

  public Class< ? extends Annotation> getScope() {
    return ThreadScoped.class;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Contextual<T> contextual) {
    Bean<T> bean = (Bean<T>) contextual;
    String variableName = bean.getName();
    Object variable = getBeanStore().getContextualInstance(variableName);
    if (variable != null) {
      return (T) variable;
    } else {
      return null;
    }
  }

  private CachingBeanStore getBeanStore() {
    CachingBeanStore cachingBeanStore = beanStore.get();
    if(cachingBeanStore == null) {
      cachingBeanStore = new CachingBeanStore();
      beanStore.set(cachingBeanStore);
    }
    return cachingBeanStore;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Contextual<T> contextual, CreationalContext<T> arg1) {
    Bean<T> bean = (Bean<T>) contextual;
    String variableName = bean.getName();
    Object variable = getBeanStore().getContextualInstance(variableName);
    if (variable != null) {
      return (T) variable;
    } else {
      T beanInstance = bean.create(arg1);
      getBeanStore().put(variableName, beanInstance);
      return beanInstance;
    }
  }

  @Override
  public boolean isActive() {
    return true;
  }

  public void clear() {
    // clean up the thread local
    beanStore.remove();
  }  

}
