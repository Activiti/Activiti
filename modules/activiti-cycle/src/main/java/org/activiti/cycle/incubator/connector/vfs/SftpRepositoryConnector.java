package org.activiti.cycle.incubator.connector.vfs;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.RepositoryException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;

public class SftpRepositoryConnector extends VfsConnector {

	private static Logger log = Logger.getLogger(SftpRepositoryConnector.class.getName());

	public SftpRepositoryConnector(VfsConnectorConfiguration configuration) {
		super(configuration);
	}

	protected void validateConfiguration() {
		if (getConfiguration().getProtocol() == null)
			throw new RepositoryException("No protocol specified");
		if (!getConfiguration().getProtocol().equals("sftp"))
			throw new RepositoryException("Protocol needs to be sftp");
		if (getConfiguration().getRepositoryPath() == null)
			throw new RepositoryException("No repositoryLocation specified");
	}

	public boolean login(String username, String password) {

		try {
			getFileSystemManager();

			connectionString = getConfiguration().getProtocol();
			connectionString += "://";
			connectionString += username + ":";
			connectionString += password;
			connectionString += "@";
			connectionString += getConfiguration().getRepositoryPath();

			// try to login:
			fileSystemManager.resolveFile(connectionString, fileSystemOptions);

		} catch (Exception e) {
			log.log(Level.WARNING, "Could not login to " + getConfiguration().getRepositoryPath(), e);
			return false;
		}

		return true;
	}

	public FileSystemManager getFileSystemManager() {
		if (fileSystemManager == null) {
			try {
				fileSystemOptions = new FileSystemOptions();
				SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fileSystemOptions, "no");
				fileSystemManager = VFS.getManager();
			} catch (Exception e) {
				throw new RepositoryException("Cannot initialize FileSystemManager: " + e.getMessage(), e);
			}
		}
		return fileSystemManager;
	}

}
