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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start new process event handler for playback purposes
 *
 * @author martin.grofcik
 */
public class DeployResourcesEventHandler implements SimulationEventHandler {

	private static Logger log = LoggerFactory.getLogger(DeployResourcesEventHandler.class);

	/** process to start key */
	protected String resourcesKey;

  public DeployResourcesEventHandler(String resourcesKey) {
    this.resourcesKey = resourcesKey;
  }


  @Override
	public void init() {
	}

	@Override
	public void handle(SimulationEvent event) {

    @SuppressWarnings("unchecked")
    Map<String, ResourceEntity> resources = (Map<String, ResourceEntity>) event.getProperty(resourcesKey);

    List<InputStream> inputStreams = new ArrayList<InputStream>();

    try {
      DeploymentBuilder deploymentBuilder = SimulationRunContext.getRepositoryService().createDeployment();

      for (ResourceEntity resource : resources.values()) {
        log.debug("adding resource [{}] to deploymnet", resource.getName());
        InputStream is = new ByteArrayInputStream(resource.getBytes());
        deploymentBuilder.addInputStream(resource.getName(), is);
        inputStreams.add(is);
      }

      deploymentBuilder.deploy();
    } finally {
      for (InputStream is : inputStreams) {
        try {
          is.close();
        } catch (IOException e) {
          log.error("Unable to close resource input stream {}", is);
        }
      }
    }
	}

}
