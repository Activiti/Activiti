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
package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

/**
 * @author Joram Barrez
 */
public abstract class AbstractOperation implements Runnable {

	protected CommandContext commandContext;
    protected Agenda agenda;
    protected ActivityExecution execution;

    public AbstractOperation() {

    }

    public AbstractOperation(CommandContext commandContext, ActivityExecution execution) {
    	this.commandContext = commandContext;
        this.execution = execution;
        this.agenda = commandContext.getAgenda();
    }

    /**
     * Helper method to match the activityId of an execution with a FlowElement
     * of the process definition referenced by the execution.
     */
    protected FlowElement findCurrentFlowElement(final ActivityExecution execution) {
        String processDefinitionId = execution.getProcessDefinitionId();
        org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
        String activityId = execution.getCurrentActivityId();
        FlowElement currentFlowElement = process.getFlowElement(activityId, true);
        execution.setCurrentFlowElement(currentFlowElement);
        return currentFlowElement;
    }
    
    /* TODO: Should following methods be moved to the entityManager */
    
    public CommandContext getCommandContext() {
		return commandContext;
	}

	public void setCommandContext(CommandContext commandContext) {
		this.commandContext = commandContext;
	}

	public Agenda getAgenda() {
        return agenda;
    }

    public void setAgenda(Agenda agenda) {
        this.agenda = agenda;
    }

    public ActivityExecution getExecution() {
        return execution;
    }

    public void setExecution(ActivityExecution execution) {
        this.execution = execution;
    }

}
