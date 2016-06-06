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

import java.util.LinkedList;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For each API call (and thus {@link Command}) being executed, a new agenda instance is created.
 * On this agenda, operations are put, which the {@link CommandExecutor} will keep executing until
 * all are executed.
 * 
 * The agenda also gives easy access to methods to plan new operations when writing 
 * {@link ActivityBehavior} implementations.
 * 
 * During a {@link Command} execution, the agenda can always be fetched using {@link Context#getAgenda()}.
 * 
 * @author Joram Barrez
 */
public class Agenda {

  private static final Logger logger = LoggerFactory.getLogger(Agenda.class);

  protected CommandContext commandContext;

  protected LinkedList<Runnable> operations = new LinkedList<Runnable>();

  public Agenda(CommandContext commandContext) {
    this.commandContext = commandContext;
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
    planOperation(operation, null);
  }

  /**
   * Generic method to plan a {@link Runnable}.
   */
  public void planOperation(Runnable operation, ExecutionEntity executionEntity) {
    operations.add(operation);
    logger.debug("Operation {} added to agenda", operation.getClass());

    if (executionEntity != null) {
      commandContext.addInvolvedExecution(executionEntity);
    }
  }

  /* SPECIFIC operations */

  public void planContinueProcessOperation(ExecutionEntity execution) {
    planOperation(new ContinueProcessOperation(commandContext, execution), execution);
  }

  public void planContinueProcessSynchronousOperation(ExecutionEntity execution) {
    planOperation(new ContinueProcessOperation(commandContext, execution, true, false), execution);
  }
  
  public void planContinueProcessInCompensation(ExecutionEntity execution) {
    planOperation(new ContinueProcessOperation(commandContext, execution, false, true), execution);
  }
  
  public void planContinueMultiInstanceOperation(ExecutionEntity execution) {
    planOperation(new ContinueMultiInstanceOperation(commandContext, execution), execution);
  }

  public void planTakeOutgoingSequenceFlowsOperation(ExecutionEntity execution, boolean evaluateConditions) {
    planOperation(new TakeOutgoingSequenceFlowsOperation(commandContext, execution, evaluateConditions), execution);
  }
  
  public void planEndExecutionOperation(ExecutionEntity execution) {
    planOperation(new EndExecutionOperation(commandContext, execution), execution);
  }

  public void planTriggerExecutionOperation(ExecutionEntity execution) {
    planOperation(new TriggerExecutionOperation(commandContext, execution), execution);
  }

  public void planDestroyScopeOperation(ExecutionEntity execution) {
    planOperation(new DestroyScopeOperation(commandContext, execution), execution);
  }
  
  public void planExecuteInactiveBehaviorsOperation() {
    planOperation(new ExecuteInactiveBehaviorsOperation(commandContext));
  }

  public CommandContext getCommandContext() {
    return commandContext;
  }

  public void setCommandContext(CommandContext commandContext) {
    this.commandContext = commandContext;
  }

  public LinkedList<Runnable> getOperations() {
    return operations;
  }

}
