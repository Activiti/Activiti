/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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


package org.activiti.spring;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.activiti.engine.ActivitiException;
import org.springframework.context.ApplicationContext;

/**


 */
public class ApplicationContextElResolver extends ELResolver {

  protected ApplicationContext applicationContext;

  public ApplicationContextElResolver(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public Object getValue(ELContext context, Object base, Object property) {
    if (base == null) {
      // according to javadoc, can only be a String
      String key = (String) property;

      if (applicationContext.containsBean(key)) {
        context.setPropertyResolved(true);
        return applicationContext.getBean(key);
      }
    }

    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
    if (base == null) {
      String key = (String) property;
      if (applicationContext.containsBean(key)) {
        throw new ActivitiException("Cannot set value of '" + property + "', it resolves to a bean defined in the Spring application-context.");
      }
    }
  }

  public Class<?> getCommonPropertyType(ELContext context, Object arg) {
    return Object.class;
  }

  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object arg) {
    return null;
  }

  public Class<?> getType(ELContext context, Object arg1, Object arg2) {
    return Object.class;
  }
}
