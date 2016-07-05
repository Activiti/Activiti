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

package org.activiti.form.engine.impl.persistence;

import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.context.Context;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntityManager;
import org.activiti.form.engine.impl.persistence.entity.FormEntityManager;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntityManager;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public abstract class AbstractManager {
  
  protected FormEngineConfiguration formEngineConfiguration;
  
  public AbstractManager(FormEngineConfiguration formEngineConfiguration) {
    this.formEngineConfiguration = formEngineConfiguration;
  }
  
  // Command scoped 
  
  protected CommandContext getCommandContext() {
    return Context.getCommandContext();
  }

  protected <T> T getSession(Class<T> sessionClass) {
    return getCommandContext().getSession(sessionClass);
  }
  
  // Engine scoped
  
  protected FormEngineConfiguration getFormEngineConfiguration() {
    return formEngineConfiguration;
  }

  protected FormDeploymentEntityManager getDeploymentEntityManager() {
    return getFormEngineConfiguration().getDeploymentEntityManager();
  }
  
  protected FormEntityManager getFormEntityManager() {
    return getFormEngineConfiguration().getFormEntityManager();
  }

  protected ResourceEntityManager getResourceEntityManager() {
    return getFormEngineConfiguration().getResourceEntityManager();
  }

}
