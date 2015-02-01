/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.activiti.osgi.blueprint;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Set;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.osgi.service.blueprint.container.BlueprintContainer;

/**
 * Custom ELResolver to resolve beans defined in the activiti-engine host
 * bundle's blueprint context.
 * 
 * @see org.activiti.spring.ApplicationContextElResolver
 * @author Prabhat_Tripathi
 * 
 *         blueprint context declaration syntax: 
 *         <bean id="blueprintContextELResolver" 
 *             class="org.activiti.osgi.blueprint.BlueprintContextELResolver">
 *           <property name="blueprintContainer" ref="blueprintContainer"/>
 *         </bean>
 */
public class BlueprintContextELResolver extends ELResolver {

  private BlueprintContainer blueprintContainer;

  public BlueprintContextELResolver() {

  }

  public BlueprintContextELResolver(BlueprintContainer blueprintContainer) {
    this.blueprintContainer = blueprintContainer;
  }

  public void setBlueprintContainer(BlueprintContainer blueprintContainer) {
    this.blueprintContainer = blueprintContainer;
  }

  @SuppressWarnings("unchecked")
  public Object getValue(ELContext context, Object base, Object property) {
    if (base == null) {
      // according to javadoc, can only be a String
      String key = (String) property;
      for (String componentId : (Set<String>) blueprintContainer.getComponentIds()) {
        if (componentId.equals(key)) {
          context.setPropertyResolved(true);
          return blueprintContainer.getComponentInstance(key);
        }
      }
    }

    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  public void setValue(ELContext context, Object base, Object property,
      Object value) {
  }

  public Class<?> getCommonPropertyType(ELContext context, Object arg) {
    return Object.class;
  }

  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
      Object arg) {
    return null;
  }

  public Class<?> getType(ELContext context, Object arg1, Object arg2) {
    return Object.class;
  }
}
