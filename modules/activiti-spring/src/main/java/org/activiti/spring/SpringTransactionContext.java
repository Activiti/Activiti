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

package org.activiti.spring;

import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class SpringTransactionContext implements TransactionContext {

  protected PlatformTransactionManager transactionManager;
  protected CommandContext commandContext;
  
  public SpringTransactionContext(PlatformTransactionManager transactionManager, CommandContext commandContext) {
    this.transactionManager = transactionManager;
    this.commandContext = commandContext;
  }
  
  public void commit() {
    // Do nothing, transaction is managed by spring
  }

  public void rollback() {
    // Just in case the rollback isn't triggered by an
    // exception, we mark the current transaction rollBackOnly.
    transactionManager.getTransaction(null).setRollbackOnly();
  }

  public void addTransactionListener(final TransactionState transactionState, final TransactionListener transactionListener) {
    if (transactionState.equals(TransactionState.COMMITTING)) {
      
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void beforeCommit(boolean readOnly) {
          transactionListener.execute(commandContext);
        }
      });
      
    } else if (transactionState.equals(TransactionState.COMMITTED)) {
    
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
          transactionListener.execute(commandContext);
        }
      });
      
    } else if (transactionState.equals(TransactionState.ROLLINGBACK)) {
      
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void beforeCompletion() {
          transactionListener.execute(commandContext);
        }
      });
      
    } else if (transactionState.equals(TransactionState.ROLLED_BACK)) {
      
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void afterCompletion(int status) {
          transactionListener.execute(commandContext);
        }
      });
      
    }
    
  }
  
  protected abstract class TransactionSynchronizationAdapter implements TransactionSynchronization {

    public void suspend() {
    }

    public void resume() {
    }

    public void flush() {
    }

    public void beforeCommit(boolean readOnly) {
    }

    public void beforeCompletion() {
    }

    public void afterCommit() {
    }

    public void afterCompletion(int status) {
    }
    
  }

}
