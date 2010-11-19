package org.activiti.cycle.incubator.connector.vfs;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;

public class VfsConnectorConfiguration extends PasswordEnabledRepositoryConnectorConfiguration {

	private String repositoryPath;

	private String protocol;

	public VfsConnectorConfiguration(String name, String repositoryLocation, String protocol) {
		setRepositoryPath(repositoryLocation);
		setName(name);
		setProtocol(protocol);
	}

	public VfsConnectorConfiguration() {

	}

	public RepositoryConnector createConnector() {
		if(protocol.equals("sftp")) {
			return new SftpRepositoryConnector(this);
		}
				
		throw new RepositoryException("No connector found for protocol " + protocol);		
	}

	public ArtifactType getDefaultArtifactType() {
		return getArtifactType(VfsConnectorPluginDefinition.ARTIFACT_TYPE_DEFAULT);
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(String repositoryLocation) {
		this.repositoryPath = repositoryLocation;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}
