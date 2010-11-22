package org.activiti.cycle.impl.connector.util;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.TransactionalRepositoryConnector;

/**
 * Utils for working with transactional connectors
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
public class TransactionalConnectorUtils {

	/**
	 * Calls the
	 * {@link TransactionalRepositoryConnector#commitPendingChanges(String)}
	 * method if connector is an instance of
	 * {@link TransactionalRepositoryConnector}. Does nothing otherwise.
	 * 
	 * @param connector
	 * @param comment
	 */
	public static void commitPendingChanges(RepositoryConnector connector, String comment) {
		if (!(connector instanceof TransactionalRepositoryConnector)) {
			// not transactional
			return;
		}
		TransactionalRepositoryConnector transactionalRepositoryConnector = (TransactionalRepositoryConnector) connector;
		transactionalRepositoryConnector.commitPendingChanges(comment);
	}

	/**
	 * Calls the {@link TransactionalRepositoryConnector#rollbackTransaction()}
	 * method if connector is instance of
	 * {@link TransactionalRepositoryConnector}. Does nothing otherwise.
	 * 
	 * @param connector
	 */
	public static void rollbackTransaction(RepositoryConnector connector) {
		if (!(connector instanceof TransactionalRepositoryConnector)) {
			// not transactional
			return;
		}
		TransactionalRepositoryConnector transactionalRepositoryConnector = (TransactionalRepositoryConnector) connector;
		transactionalRepositoryConnector.rollbackTransaction();
	}

	/**
	 * Calls the {@link TransactionalRepositoryConnector#beginTransaction()} if
	 * connector is an instance of {@link TransactionalRepositoryConnector}.
	 * Does nothing otherwise.
	 * 
	 * @param connector
	 * @param message
	 */
	public static void beginTransaction(RepositoryConnector connector) {
		if (!(connector instanceof TransactionalRepositoryConnector)) {
			// not transactional
			return;
		}
		TransactionalRepositoryConnector transactionalRepositoryConnector = (TransactionalRepositoryConnector) connector;
		transactionalRepositoryConnector.beginTransaction();
	}

}
