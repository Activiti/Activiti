/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.cfg.jta;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;


public class JtaTransactionContext implements TransactionContext {

  protected final TransactionManager transactionManager;

  public JtaTransactionContext(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void commit() {
    // managed transaction, ignore
  }

  public void rollback() {
    // managed transaction, mark rollback-only if not done so already.
    try {
      Transaction transaction = getTransaction();
      int status = transaction.getStatus();
      if (status != Status.STATUS_NO_TRANSACTION && status != Status.STATUS_ROLLEDBACK) {
        transaction.setRollbackOnly();
      }
    } catch (IllegalStateException e) {
      throw new ActivitiException("Unexpected IllegalStateException while marking transaction rollback only");
    } catch (SystemException e) {
      throw new ActivitiException("SystemException while marking transaction rollback only");
    }
  }

  protected Transaction getTransaction() {
    try {
      return transactionManager.getTransaction();
    } catch (SystemException e) {
      throw new ActivitiException("SystemException while getting transaction ", e);
    }
  }

  public void addTransactionListener(TransactionState transactionState, final TransactionListener transactionListener) {
    Transaction transaction = getTransaction();
    CommandContext commandContext = Context.getCommandContext();
    try {
      transaction.registerSynchronization(new TransactionStateSynchronization(transactionState, transactionListener, commandContext));
    } catch (IllegalStateException e) {
      throw new ActivitiException("IllegalStateException while registering synchronization ", e);
    } catch (RollbackException e) {
      throw new ActivitiException("RollbackException while registering synchronization ", e);
    } catch (SystemException e) {
      throw new ActivitiException("SystemException while registering synchronization ", e);
    }
  }

  public static class TransactionStateSynchronization implements Synchronization {

    protected final TransactionListener transactionListener;
    protected final TransactionState transactionState;
    private final CommandContext commandContext;

    public TransactionStateSynchronization(TransactionState transactionState, TransactionListener transactionListener, CommandContext commandContext) {
      this.transactionState = transactionState;
      this.transactionListener = transactionListener;
      this.commandContext = commandContext;
    }

    public void beforeCompletion() {
      if (TransactionState.COMMITTING.equals(transactionState) || TransactionState.ROLLINGBACK.equals(transactionState)) {
        transactionListener.execute(commandContext);
      }
    }

    public void afterCompletion(int status) {
      if (Status.STATUS_ROLLEDBACK == status && TransactionState.ROLLED_BACK.equals(transactionState)) {
        transactionListener.execute(commandContext);
      } else if (Status.STATUS_COMMITTED == status && TransactionState.COMMITTED.equals(transactionState)) {
        transactionListener.execute(commandContext);
      }
    }

  }

}
