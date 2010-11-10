package org.activiti.cycle.impl.connector.svn;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

/**
 * The Configuration for {@link SvnRepositoryConnector}s
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
public class SvnConnectorConfiguration extends PasswordEnabledRepositoryConnectorConfiguration {

	protected String repositoryPath = "";

	public SvnConnectorConfiguration() {
	}

	public SvnConnectorConfiguration(String name, String repoLocation) {
		repositoryPath = repoLocation;
		setName(name);
	}

	/**
	 * creates and configures a new {@link SvnRepositoryConnector}
	 * 
	 * @return
	 */
	public RepositoryConnector createConnector() {
		SvnRepositoryConnector theConnector = new SvnRepositoryConnector(this);
		return theConnector;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

}
