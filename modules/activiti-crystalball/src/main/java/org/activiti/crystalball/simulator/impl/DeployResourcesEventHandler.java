package org.activiti.crystalball.simulator.impl;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.test.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
