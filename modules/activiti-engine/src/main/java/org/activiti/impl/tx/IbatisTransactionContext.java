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
package org.activiti.impl.tx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.impl.persistence.db.DbSqlSession;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistenceSession;


/**
 * @author Tom Baeyens
 */
public class IbatisTransactionContext implements TransactionContext {
  
  private static Logger log = Logger.getLogger(IbatisTransactionContext.class.getName());

  CommandContext commandContext;
  Map<TransactionState,List<TransactionListener>> stateTransactionListeners = null;
  
  public IbatisTransactionContext(CommandContext commandContext) {
    this.commandContext = commandContext;
    log.fine("created new ibatis transaction context");
  }

  public void addTransactionListener(TransactionState transactionState, TransactionListener transactionListener) {
    if (stateTransactionListeners==null) {
      stateTransactionListeners = new HashMap<TransactionState, List<TransactionListener>>();
    }
    List<TransactionListener> transactionListeners = stateTransactionListeners.get(transactionState);
    if (transactionListeners==null) {
      transactionListeners = new ArrayList<TransactionListener>();
      stateTransactionListeners.put(transactionState, transactionListeners);
    }
    transactionListeners.add(transactionListener);
  }
  
  public void commit() {
    log.fine("firing event committing...");
    fireTransactionEvent(TransactionState.COMMITTING);
    log.fine("committing the ibatis sql session...");
    getPersistenceSession().commit();
    getDbSqlSession().commit();
    log.fine("firing event committed...");
    fireTransactionEvent(TransactionState.COMMITTED);
  }

  protected void fireTransactionEvent(TransactionState transactionState) {
    if (stateTransactionListeners==null) {
      return;
    }
    List<TransactionListener> transactionListeners = stateTransactionListeners.get(transactionState);
    if (transactionListeners==null) {
      return;
    }
    for (TransactionListener transactionListener: transactionListeners) {
      transactionListener.execute(commandContext);
    }
  }

  private PersistenceSession getPersistenceSession() {
    return commandContext.getPersistenceSession();
  }

  private DbSqlSession getDbSqlSession() {
    return commandContext.getSession(DbSqlSession.class);
  }

  public void rollback() {
    try {
      try {
        log.fine("firing event rolling back...");
        fireTransactionEvent(TransactionState.ROLLINGBACK);
        
      } catch (Throwable exception) {
        log.info("Exception during transaction: " + exception.getMessage());
        commandContext.exception(exception);
      } finally {
        log.fine("rolling back ibatis sql session...");
        getPersistenceSession().rollback();
        getDbSqlSession().rollback();
      }
      
    } catch (Throwable exception) {
      log.info("Exception during transaction: " + exception.getMessage());
      commandContext.exception(exception);

    } finally {
      log.fine("firing event rolled back...");
      fireTransactionEvent(TransactionState.ROLLED_BACK);
    }
  }
}
