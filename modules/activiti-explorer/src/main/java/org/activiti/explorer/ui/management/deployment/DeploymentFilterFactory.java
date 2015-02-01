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

package org.activiti.explorer.ui.management.deployment;


import java.io.Serializable;

import org.activiti.explorer.ComponentFactories;
import org.activiti.explorer.ui.ComponentFactory;
import org.activiti.explorer.ui.process.DefaultProcessDefinitionFilter;


/**
 * Factory class for the {@link DeploymentFilter} to use when populating deployment list.
 * Regardless of the environment, initialized with a {@link DefaultProcessDefinitionFilter}. 
 * 
 * If another filter-impl is needed, this needs to be configured in the environment using the setter-method
 * accessible on this class.
 * 
 * @author Frederik Heremans
 */
public class DeploymentFilterFactory implements ComponentFactory<DeploymentFilter>, Serializable {

  private static final long serialVersionUID = 6401451647516737141L;
  
  private DeploymentFilter deploymentFilter;

  public void initialise(String environment) {
    // Create default component. Environment-specific will be set when needed.
    if(deploymentFilter == null) {
      deploymentFilter = new DefaultDeploymentFilter();
    }
  }
  
  /**
   * @param ComponentFactories the factories that is used to register this class to,
   * when set.
   */
  public void setComponentFactories(ComponentFactories componentFactories) {
    componentFactories.add(DeploymentFilterFactory.class, this);
  }

  public DeploymentFilter create() {
    return deploymentFilter;
  }
  
  public void setDeploymentFilter(DeploymentFilter deploymentFilter)
  {
    this.deploymentFilter = deploymentFilter;
  }
}
