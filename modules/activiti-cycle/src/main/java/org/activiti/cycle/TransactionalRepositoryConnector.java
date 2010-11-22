package org.activiti.cycle;

public interface TransactionalRepositoryConnector extends RepositoryConnector {

	/**
	 * Some connectors support transactions. Transactions are sets of
	 * modifications to the repository that either succeed as a whole of fail as
	 * a wohle.
	 * 
	 * It is good practice to call beginTransaction() before making a set of
	 * modifications to the repository and then try to commit them using
	 * {@link #commitPendingChanges(String)}.
	 * 
	 * The {@link #beginTransaction()} has the following behavior:
	 * <ul>
	 * <li>if a transaction is already running the method returns</li>
	 * <li>if no transaction is running, the method starts a new transaction</li>
	 * </ul>
	 * 
	 * What a transaction 'means' is dependent individual connectors; most connectors will not  
	 * 
	 */
	public void beginTransaction();
	
	public void rollbackTransaction();
	
	/**
	 * Some connectors support commit (like SVN), so all pending changes must be
	 * committed correctly. If the connector doesn't support committing, this
	 * method just does nothing. This means, there is no rollback and you
	 * shouldn't rely on a transaction behavior.
	 * 
	 * TODO: Should be change the name that it fits to the beginTransaction
	 * method?
	 * 
	 * TODO: Do we need a rollbackTransaction method?
	 */
	public void commitPendingChanges(String comment);
	
}
