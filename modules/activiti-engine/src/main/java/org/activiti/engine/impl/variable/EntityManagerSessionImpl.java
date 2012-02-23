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

package org.activiti.engine.impl.variable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class EntityManagerSessionImpl implements EntityManagerSession {

  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;
  private boolean handleTransactions;
  private boolean closeEntityManager;
  
  public EntityManagerSessionImpl(EntityManagerFactory entityManagerFactory, EntityManager entityManager, 
          boolean handleTransactions, boolean closeEntityManager) {
    this(entityManagerFactory, handleTransactions, closeEntityManager);
    this.entityManager = entityManager;
  }
  
  public EntityManagerSessionImpl(EntityManagerFactory entityManagerFactory, boolean handleTransactions, boolean closeEntityManager) {
    this.entityManagerFactory = entityManagerFactory;
    this.handleTransactions = handleTransactions;
    this.closeEntityManager = closeEntityManager;
  }

  public void flush() {
    if (entityManager != null && (!handleTransactions || isTransactionActive()) ) {
      try {
        entityManager.flush();
      } catch (IllegalStateException ise) {
        throw new ActivitiException("Error while flushing EntityManager, illegal state", ise);
      } catch (TransactionRequiredException tre) {
        throw new ActivitiException("Cannot flush EntityManager, an active transaction is required", tre);
      } catch (PersistenceException pe) {
        throw new ActivitiException("Error while flushing EntityManager: " + pe.getMessage(), pe);
      }
    }
  }

  protected boolean isTransactionActive() {
    if (handleTransactions && entityManager.getTransaction() != null) {
      return entityManager.getTransaction().isActive();
    }
    return false;
  }

  public void close() {
    if (closeEntityManager && entityManager != null && !entityManager.isOpen()) {
      try {
        entityManager.close();
      } catch (IllegalStateException ise) {
        throw new ActivitiException("Error while closing EntityManager, may have already been closed or it is container-managed", ise);
      }
    }
  }

  public EntityManager getEntityManager() {
    if (entityManager == null) {
      entityManager = getEntityManagerFactory().createEntityManager();
      
      if(handleTransactions) {
        // Add transaction listeners, if transactions should be handled
        TransactionListener jpaTransactionCommitListener = new TransactionListener() {
          public void execute(CommandContext commandContext) {
            if (isTransactionActive()) {
              entityManager.getTransaction().commit();
            }
          }
        };
        
        TransactionListener jpaTransactionRollbackListener = new TransactionListener() {
          public void execute(CommandContext commandContext) {
            if (isTransactionActive()) {
              entityManager.getTransaction().rollback();
            }
          }
        };

        TransactionContext transactionContext = Context.getCommandContext().getTransactionContext();
        transactionContext.addTransactionListener(TransactionState.COMMITTED, jpaTransactionCommitListener);
        transactionContext.addTransactionListener(TransactionState.ROLLED_BACK, jpaTransactionRollbackListener);

        // Also, start a transaction, if one isn't started already
        if (!isTransactionActive()) {
          entityManager.getTransaction().begin();
        }
      }
    }
    
    return entityManager;
  }

  private EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }
}
