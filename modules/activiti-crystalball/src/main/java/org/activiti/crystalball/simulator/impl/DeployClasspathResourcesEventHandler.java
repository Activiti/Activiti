package org.activiti.crystalball.simulator.impl;

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


import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class deploys resources from classpath
 */
public class DeployClasspathResourcesEventHandler implements SimulationEventHandler {

  private static Logger log = LoggerFactory.getLogger(DeployClasspathResourcesEventHandler.class);

  /**
   * process to start key
   */
  protected String resourcesKey;

  public DeployClasspathResourcesEventHandler(String resourcesKey) {
    this.resourcesKey = resourcesKey;
  }


  @Override
  public void init() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handle(SimulationEvent event) {

    List<String> resources = (List<String>) event.getProperty(resourcesKey);

    DeploymentBuilder deploymentBuilder = SimulationRunContext.getRepositoryService().createDeployment();

    for (String resource : resources) {
      log.debug("adding resource [{}] to repository", resource, SimulationRunContext.getRepositoryService());
      deploymentBuilder.addClasspathResource(resource);
    }

    deploymentBuilder.deploy();
  }

}
