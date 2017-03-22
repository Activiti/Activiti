package org.activiti.examples.runtime;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.agenda.DefaultActivitiAgenda;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.runtime.ActivitiAgenda;
import org.activiti.engine.impl.runtime.Agenda;
import org.springframework.beans.factory.FactoryBean;

/**
 * This class implements an factory for generating watchdog agenda
 */
public class WatchDogAgendaFactory implements FactoryBean<ActivitiAgenda> {

    @Override
    public ActivitiAgenda getObject() throws Exception {
        return new WatchDogAgenda(new DefaultActivitiAgenda());
    }

    @Override
    public Class<?> getObjectType() {
        return WatchDogAgenda.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    private static class WatchDogAgenda implements ActivitiAgenda {

        private static final int WATCH_DOG_LIMIT = 10;

        private final ActivitiAgenda agenda;
        private int counter;

        private WatchDogAgenda(ActivitiAgenda agenda) {
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
		public void planOperation(Runnable operation, ExecutionEntity executionEntity) {
			agenda.planOperation(operation, executionEntity);
			
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
