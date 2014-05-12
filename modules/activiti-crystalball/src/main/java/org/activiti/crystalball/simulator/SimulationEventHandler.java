package org.activiti.crystalball.simulator;

/**
 * @author martin.grofcik
 */
public interface SimulationEventHandler {

	/**
	 * initialize event handler
	 */
	void init();
	
	/**
	 * execute event in the context
	 * @param event event to handle
	 */
	void handle(SimulationEvent event);

}
