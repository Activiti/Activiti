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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;

/**
 * {@link ProcessEngineLookup} for looking up or building a local
 * {@link ProcessEngine} using the provided processEngineName (
 * {@link ProcessEngines#NAME_DEFAULT} is used as default value). Can be used
 * for looking up existing {@link ProcessEngine}s in the same ClassLoader as
 * this Class.
 * <p />
 * Use this Strategy if you want to build and manage a {@link ProcessEngine}
 * local to your application.
 * <p />
 * Note: Requires an "activiti.cfg.xml" to be available on the classpath.
 * 
 * @author Daniel Meyer
 */
public class LocalProcessEngineLookup implements org.activiti.cdi.spi.ProcessEngineLookup {
  
  public int getPrecedence() {
    return 10;
  }

  protected String processEngineName = ProcessEngines.NAME_DEFAULT;

  @Override
  public ProcessEngine getProcessEngine() {
    return ProcessEngines.getProcessEngine(getProcessEngineName());
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  @Override
  public void ungetProcessEngine() {
    try {
      ProcessEngines.getProcessEngine(getProcessEngineName()).close();
    } catch (Exception e) {
      throw new ActivitiException("Unable to close the local ProcessEngine", e);
    }
  }
}
