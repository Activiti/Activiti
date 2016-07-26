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
package org.activiti.form.engine.configurator;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.FormEngine;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.deployer.FormDeployer;

/**
 * @author Tijs Rademakers
 */
public class FormEngineConfigurator extends AbstractProcessEngineConfigurator {
  
  protected static FormEngine formEngine;
  protected FormEngineConfiguration formEngineConfiguration;
  
  @Override
  public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    initFormEngine();
    
    processEngineConfiguration.setFormEngineInitialized(true);
    processEngineConfiguration.setFormEngineRepositoryService(formEngine.getFormRepositoryService());
    processEngineConfiguration.setFormEngineFormService(formEngine.getFormService());
    
    List<Deployer> deployers = null;
    if (processEngineConfiguration.getCustomPostDeployers() != null) {
      deployers = processEngineConfiguration.getCustomPostDeployers();
    } else {
      deployers = new ArrayList<Deployer>();
    }
    
    deployers.add(new FormDeployer());
    processEngineConfiguration.setCustomPostDeployers(deployers);
  }

  protected synchronized void initFormEngine() {
    if (formEngine == null) {
      if (formEngineConfiguration == null) {
        throw new ActivitiFormException("FormEngineConfiguration is required");
      }
      
      formEngine = formEngineConfiguration.buildFormEngine();
    }
  }

  public FormEngineConfiguration getFormEngineConfiguration() {
    return formEngineConfiguration;
  }

  public FormEngineConfigurator setFormEngineConfiguration(FormEngineConfiguration formEngineConfiguration) {
    this.formEngineConfiguration = formEngineConfiguration;
    return this;
  }

}
