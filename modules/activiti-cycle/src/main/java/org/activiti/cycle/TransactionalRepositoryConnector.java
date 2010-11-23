package org.activiti.cycle;

/**
 * Interface for {@link RepositoryConnector} supporting transactions.
 * Transactions are sets of modifications to the repository that either succeed
 * as a whole of fail as a whole.
 * 
 * It is good practice to call beginTransaction() before making a set of
 * modifications to the repository and then try to commit them using
 * {@link #commitTransaction(String)}.
 * 
 * @author daniel.meyer@camunda.com
 * @see TransactionalConnectorUtils
 */
public interface TransactionalRepositoryConnector extends RepositoryConnector {

  /**
   * Starts a new transaction. The {@link #beginTransaction()} has the following
   * behavior:
   * <ul>
   * <li>if a transaction is already running the method returns</li>
   * <li>if no transaction is running, the method starts a new transaction</li>
   * </ul>
   * Contract: the user must either call {@link #commitTransaction(String)}
   * or {@link #rollbackTransaction()}.
   * 
   * @see TransactionalConnectorUtils#beginTransaction(RepositoryConnector)
   */
  public void beginTransaction();

  /**
   * Performs a rollback for a running transaction. Causes a connector to
   * release all resources related to the active transaction (i.e. temporary
   * files)
   * 
   * @see TransactionalConnectorUtils#rollbackTransaction(RepositoryConnector)
   */
  public void rollbackTransaction();

  /**
   * Commits pending changes. Contract: if the commit fails, the user must
   * {@link #rollbackTransaction()}.
   * 
   * @see TransactionalConnectorUtils#commitTransaction(RepositoryConnector,
   *      String)
   */
  public void commitTransaction(String comment);

}
