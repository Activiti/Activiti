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
package org.activiti.engine.impl.operation;

import java.util.LinkedList;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class Agenda {
	
	private static final Logger logger = LoggerFactory.getLogger(Agenda.class);
	
	protected LinkedList<Runnable> operations = new LinkedList<Runnable>();
	
	public Agenda() {
		
	}
	
	public boolean isEmpty() {
		return operations.isEmpty();
	}
	
	public Runnable getNextOperation() {
		return operations.poll();
	}
	
	/**
	 * Generic method to plan a {@link Runnable}.
	 */
	public void planOperation(Runnable operation) {
		operations.add(operation);
		logger.debug("Operation {} added to agenda", operation.getClass());
	}
	
	/* SPECIFIC operations */
	
	public void planContinueProcessOperation(ActivityExecution execution) {
		planOperation(new ContinueProcessOperation(this, execution));
	}
	
	public void planTakeOutgoingSequenceFlowsOperation(ActivityExecution execution, boolean evaluateConditions) {
		planOperation(new TakeOutgoingSequenceFlowsOperation(this, execution, evaluateConditions));
	}
	
	public void planEndExecutionOperation(ActivityExecution execution) {
		planOperation(new EndExecutionOperation(this, execution));
	}
	

}
