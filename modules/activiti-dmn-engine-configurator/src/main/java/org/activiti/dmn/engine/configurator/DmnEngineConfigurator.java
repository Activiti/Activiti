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
package org.activiti.dmn.engine.configurator;

import java.util.ArrayList;
import java.util.List;

import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.DmnEngine;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.deployer.DmnDeployer;
import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.deploy.Deployer;

/**
 * @author Tijs Rademakers
 */
public class DmnEngineConfigurator extends AbstractProcessEngineConfigurator {
  
  protected static DmnEngine dmnEngine;
  protected DmnEngineConfiguration dmnEngineConfiguration;
  
  @Override
  public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    initDmnEngine();
    
    processEngineConfiguration.setDmnEngineInitialized(true);
    processEngineConfiguration.setDmnEngineRepositoryService(dmnEngine.getDmnRepositoryService());
    processEngineConfiguration.setDmnEngineRuleService(dmnEngine.getDmnRuleService());
    
    List<Deployer> deployers = null;
    if (processEngineConfiguration.getCustomPostDeployers() != null) {
      deployers = processEngineConfiguration.getCustomPostDeployers();
    } else {
      deployers = new ArrayList<Deployer>();
    }
    
    deployers.add(new DmnDeployer());
    processEngineConfiguration.setCustomPostDeployers(deployers);
  }

  protected synchronized void initDmnEngine() {
    if (dmnEngine == null) {
      if (dmnEngineConfiguration == null) {
        throw new ActivitiDmnException("DmnEngineConfiguration is required");
      }
      
      dmnEngine = dmnEngineConfiguration.buildDmnEngine();
    }
  }

  public DmnEngineConfiguration getDmnEngineConfiguration() {
    return dmnEngineConfiguration;
  }

  public DmnEngineConfigurator setDmnEngineConfiguration(DmnEngineConfiguration dmnEngineConfiguration) {
    this.dmnEngineConfiguration = dmnEngineConfiguration;
    return this;
  }

}
