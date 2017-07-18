
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
package org.activiti.examples.runtime;

import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.ActivitiEngineAgendaFactory;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.agenda.DefaultActivitiEngineAgenda;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * This class implements an factory for generating watchdog agenda
 */
public class WatchDogAgendaFactory implements ActivitiEngineAgendaFactory {

  @Override
  public ActivitiEngineAgenda createAgenda(CommandContext commandContext) {
    return new WatchDogAgenda(new DefaultActivitiEngineAgenda(commandContext));
  }

  private static class WatchDogAgenda implements ActivitiEngineAgenda {

    private static final int WATCH_DOG_LIMIT = 10;

    private final ActivitiEngineAgenda agenda;
    private int counter;

    private WatchDogAgenda(ActivitiEngineAgenda agenda) {
      this.agenda = agenda;
    }

    @Override
    public boolean isEmpty() {
      return agenda.isEmpty();
    }

    @Override
    public Runnable getNextOperation() {
      if (counter<WATCH_DOG_LIMIT) {
        counter++;
        return agenda.getNextOperation();
      }
      throw new ActivitiException("WatchDog limit exceeded.");
    }

    @Override
    public void planOperation(Runnable operation) {
      agenda.planOperation(operation);
    }


    @Override
    public void planContinueProcessOperation(ExecutionEntity execution) {
      agenda.planContinueProcessOperation(execution);

    }

    @Override
    public void planContinueProcessSynchronousOperation(ExecutionEntity execution) {
      agenda.planContinueProcessSynchronousOperation(execution);

    }

    @Override
    public void planContinueProcessInCompensation(ExecutionEntity execution) {
      agenda.planContinueProcessInCompensation(execution);

    }

    @Override
    public void planContinueMultiInstanceOperation(ExecutionEntity execution) {
      agenda.planContinueMultiInstanceOperation(execution);

    }

    @Override
    public void planTakeOutgoingSequenceFlowsOperation(ExecutionEntity execution, boolean evaluateConditions) {
      agenda.planTakeOutgoingSequenceFlowsOperation(execution, evaluateConditions);

    }

    @Override
    public void planEndExecutionOperation(ExecutionEntity execution) {
      agenda.planEndExecutionOperation(execution);

    }

    @Override
    public void planTriggerExecutionOperation(ExecutionEntity execution) {
      agenda.planTriggerExecutionOperation(execution);

    }

    @Override
    public void planDestroyScopeOperation(ExecutionEntity execution) {
      agenda.planDestroyScopeOperation(execution);

    }

    @Override
    public void planExecuteInactiveBehaviorsOperation() {
      agenda.planExecuteInactiveBehaviorsOperation();

    }

  }

}
