package org.activiti.cycle.incubator.connector.vfs;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.annotations.Interceptors;
import org.activiti.cycle.impl.connector.ConnectorLoginInterceptor;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;

/**
 * Connector for using SftpRepositories
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent
@Interceptors({ ConnectorLoginInterceptor.class })
public class SftpRepositoryConnector extends VfsConnector {

  private static Logger log = Logger.getLogger(SftpRepositoryConnector.class.getName());

  public SftpRepositoryConnector(VfsConnectorConfiguration configuration) {
    super(configuration);
  }

  public SftpRepositoryConnector() {
  }

  protected void validateConfiguration() {
    if (getConfiguration().getProtocol() == null)
      throw new RepositoryException("No protocol specified");
    if (!getConfiguration().getProtocol().equals("sftp"))
      throw new RepositoryException("Protocol needs to be sftp");
    if (getConfiguration().getHostname() == null)
      throw new RepositoryException("No hostname specified");
  }

  public boolean login(String username, String password) {

    try {
      getFileSystemManager();

      connectionString = getConfiguration().getProtocol();
      connectionString += "://";
      connectionString += username + ":";
      connectionString += password;
      connectionString += "@";
      connectionString += getConfiguration().getHostname();
      if (getConfiguration().getPath() != null) {
        connectionString += getConfiguration().getPath();
      }

      // try to login:
      fileSystemManager.resolveFile(connectionString, fileSystemOptions);

    } catch (Exception e) {
      log.log(Level.WARNING, "Could not login to " + getConfiguration().getHostname(), e);
      return false;
    }
    loggedIn = true;
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
