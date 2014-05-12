package org.activiti.crystalball.simulator.delegate;

import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * @author martin.grofcik
 */
public abstract class AbstractSimulationActivityBehavior implements ActivityBehavior {

	public AbstractSimulationActivityBehavior() {
	}
	
	abstract public void execute(ActivityExecution execution) throws Exception;
}
