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
package org.activiti.cdi.impl;

import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;

/**
 * <!> EXPERIMENTAL <!>
 * 
 * {@link ProcessEngineLookup} for looking up a {@link ProcessEngine} in Jndi.
 * 
 * Looks up a process engine bound to 'activiti/default'.
 * 
 * TODO: test & make configurable! 
 *  
 * Note that this in an {@link Alternative} implementation of the
 * {@link ProcessEngineLookup} interface: if you want to use this
 * implementation, enable it in the beans.xml deployment descriptor.
 * 
 * @author Daniel Meyer
 */
@Alternative
public class JndiProcessEngineLookup implements ProcessEngineLookup {

  Logger logger = Logger.getLogger(JndiProcessEngineLookup.class.getName());

  protected String jndiName;

  @Override
  public ProcessEngine getProcessEngine() {    
    try {
      Object engine = InitialContext.doLookup(getJndiName());
      logger.info("Looked up ProcessEngine '" + engine + "' in jndi (" + jndiName + ")");
      return (ProcessEngine) engine;
    } catch (NamingException e) {
      throw new ActivitiException("No Process Engine is bound to the jndi name '" + jndiName + "'.", e);
    } catch (ClassCastException e) {
      throw new ActivitiException("The object bound to '" + jndiName + "' appears not to be a ProcessEngine: " + e.getMessage(), e);
    }
  }
  public String getJndiName() {
    if (jndiName == null) {
      synchronized (this) {
        if (jndiName == null) {
          initJndiName();
        }
      }
    }
    return jndiName;
  }

  protected void initJndiName() {
    jndiName = "activiti/default";
  }

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }
  
  @Override
  public void ungetProcessEngine() {
    // do nothing    
  }

}
