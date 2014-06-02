package org.activiti.crystalball.simulator.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;

/**
 * No operation event handler 
 *
 * @author martin.grofcik
 */
public class NoopEventHandler implements SimulationEventHandler {

	@Override
	public void init() {

	}

	@Override
	public void handle(SimulationEvent event) {

	}

}
