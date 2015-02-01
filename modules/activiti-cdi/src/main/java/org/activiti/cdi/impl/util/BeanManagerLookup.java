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
package org.activiti.cdi.impl.util;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.activiti.engine.ActivitiException;

public class BeanManagerLookup {

  /** holds a local beanManager if no jndi is available */
  public static BeanManager localInstance;

  /** provide a custom jndi lookup name */
  public static String jndiName;

  public static BeanManager getBeanManager() {
    if (localInstance != null) {
      return localInstance;
    }
    return lookupBeanManagerInJndi();
  }

  private static BeanManager lookupBeanManagerInJndi() {

    if (jndiName != null) {
      try {
        return (BeanManager) InitialContext.doLookup(jndiName);
      } catch (NamingException e) {
        throw new ActivitiException("Could not lookup beanmanager in jndi using name: '" + jndiName + "'.", e);
      }
    }

    try {
      // in an application server
      return (BeanManager) InitialContext.doLookup("java:comp/BeanManager");
    } catch (NamingException e) {
      // silently ignore
    }

    try {
      // in a servlet container
      return (BeanManager) InitialContext.doLookup("java:comp/env/BeanManager");
    } catch (NamingException e) {
      // silently ignore
    }

    throw new ActivitiException(
            "Could not lookup beanmanager in jndi. If no jndi is avalable, set the beanmanger to the 'localInstance' property of this class.");
  }
}