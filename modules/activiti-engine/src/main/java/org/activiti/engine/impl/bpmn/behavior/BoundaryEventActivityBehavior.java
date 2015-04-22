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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.Collection;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.agenda.OperationUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author Joram Barrez
 */
public class BoundaryEventActivityBehavior extends FlowNodeActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    protected boolean interrupting;

    public BoundaryEventActivityBehavior() {}

    public BoundaryEventActivityBehavior(boolean interrupting) {
        this.interrupting = interrupting;
    }
    
    @Override
    public void execute(ActivityExecution execution) {
        
    }

    @Override
    public void trigger(ActivityExecution execution, String triggerName, Object triggerData) {

        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        
        CommandContext commandContext = Context.getCommandContext();

        if (interrupting) {
            executeInterruptingBehavior(executionEntity, commandContext);
        } else {
            executeNonInterruptingBehavior(executionEntity, commandContext);
        }
    }

    protected void executeInterruptingBehavior(ExecutionEntity executionEntity, CommandContext commandContext) {

        // TODO: is this needed???
        // if (executionEntity.getSubProcessInstance() != null) {
        // executionEntity.getSubProcessInstance().deleteCascade(executionEntity.getDeleteReason());
        // }

        // The destroy scope operation will look for the parent execution and
        // destroy the whole scope,
        // and leave the boundary event using this parent execution.
        //
        // The take outgoing seq flows operation below (the non-interrupting
        // else clause) on the other hand
        // uses the child execution to leave, which keeps the scope alive.
        //
        // Which is what we need.

        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        ExecutionEntity attachedRefScopeExecution = executionEntityManager.findExecutionById(executionEntity.getParentId());
        
        ExecutionEntity parentScopeExecution = null;
        ExecutionEntity currentlyExaminedExecution = executionEntityManager.findExecutionById(attachedRefScopeExecution.getParentId());
        while (currentlyExaminedExecution != null && parentScopeExecution == null) {
            if (currentlyExaminedExecution.isScope()) {
                parentScopeExecution = currentlyExaminedExecution;
            } else {
                currentlyExaminedExecution = executionEntityManager.findExecutionById(executionEntity.getParentId());
            }
        }

        if (parentScopeExecution == null) {
            throw new ActivitiException("Programmatic error: no parent scope execution found for boundary event");
        }
        
        // set new parent for boundary event execution
        executionEntity.setParent(parentScopeExecution);
        
        deleteChildExecutions(attachedRefScopeExecution, executionEntity, commandContext);
        
        commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, true);
    }

    protected void executeNonInterruptingBehavior(ExecutionEntity executionEntity, CommandContext commandContext) {

        // Non-interrupting: the current execution is given the first parent
        // scope (which isn't its direct parent)
        //
        // Why? Because this execution does NOT have anything to do with
        // the current parent execution (the one where the boundary event is
        // on): when it is deleted or whatever, this does not impact this new execution at all,
        // it is completely independent in that regard.

        // Note: if the parent of the parent does not exists, this becomes a concurrent execution in the process instance!

        ExecutionEntity parentExecutionEntity = commandContext.getExecutionEntityManager().findExecutionById(executionEntity.getParentId());

        ExecutionEntity scopeExecution = null;
        ExecutionEntity currentlyExaminedExecution = commandContext.getExecutionEntityManager().findExecutionById(parentExecutionEntity.getParentId());
        while (currentlyExaminedExecution != null && scopeExecution == null) {
            if (currentlyExaminedExecution.isScope()) {
                scopeExecution = currentlyExaminedExecution;
            } else {
                currentlyExaminedExecution = commandContext.getExecutionEntityManager().findExecutionById(currentlyExaminedExecution.getParentId());
            }
        }
        
        if (scopeExecution == null) {
            throw new ActivitiException("Programmatic error: no parent scope execution found for boundary event");
        }
       
        ExecutionEntity nonInterruptingExecution = scopeExecution.createExecution();
        nonInterruptingExecution.setCurrentFlowElement(executionEntity.getCurrentFlowElement());

        commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(nonInterruptingExecution, true);
    }

    protected void deleteChildExecutions(ExecutionEntity parentExecution, ExecutionEntity notToDeleteExecution, CommandContext commandContext) {
        // Delete all child executions
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        Collection<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
        if (CollectionUtils.isNotEmpty(childExecutions)) {
            for (ExecutionEntity childExcecution : childExecutions) {
                if (childExcecution.getId().equals(notToDeleteExecution.getId()) == false) {
                    deleteChildExecutions(childExcecution, notToDeleteExecution, commandContext);
                }
            }
        }
            
        OperationUtil.deleteDataRelatedToExecution(commandContext, parentExecution);
        commandContext.getExecutionEntityManager().delete(parentExecution);
    }
    
    public boolean isInterrupting() {
        return interrupting;
    }

    public void setInterrupting(boolean interrupting) {
        this.interrupting = interrupting;
    }

}
