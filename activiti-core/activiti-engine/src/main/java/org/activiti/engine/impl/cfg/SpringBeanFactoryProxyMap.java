/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.cfg;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.springframework.beans.factory.BeanFactory;


public class SpringBeanFactoryProxyMap implements Map<Object, Object> {

  protected BeanFactory beanFactory;

  public SpringBeanFactoryProxyMap(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public Object get(Object key) {
    if ((key == null) || (!String.class.isAssignableFrom(key.getClass()))) {
      return null;
    }
    return beanFactory.getBean((String) key);
  }

  public boolean containsKey(Object key) {
    if ((key == null) || (!String.class.isAssignableFrom(key.getClass()))) {
      return false;
    }
    return beanFactory.containsBean((String) key);
  }

  public Set<Object> keySet() {
    throw new ActivitiException("unsupported operation on configuration beans");
    // List<String> beanNames =
    // asList(beanFactory.getBeanDefinitionNames());
    // return new HashSet<Object>(beanNames);
  }

  public void clear() {
    throw new ActivitiException("can't clear configuration beans");
  }

  public boolean containsValue(Object value) {
    throw new ActivitiException("can't search values in configuration beans");
  }

  public Set<Map.Entry<Object, Object>> entrySet() {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  public boolean isEmpty() {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  public Object put(Object key, Object value) {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  public void putAll(Map<? extends Object, ? extends Object> m) {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  public Object remove(Object key) {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  public int size() {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  public Collection<Object> values() {
    throw new ActivitiException("unsupported operation on configuration beans");
  }
}
