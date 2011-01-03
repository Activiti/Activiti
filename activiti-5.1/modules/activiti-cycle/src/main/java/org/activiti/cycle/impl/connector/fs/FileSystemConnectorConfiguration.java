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
package org.activiti.cycle.impl.connector.fs;

import java.io.File;
import java.io.IOException;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

/**
 * Object used to configure FS connector. Candidate for Entity to save config
 * later on.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class FileSystemConnectorConfiguration extends RepositoryConnectorConfiguration {

  private String baseFilePath;

  public FileSystemConnectorConfiguration() {
  }

  public FileSystemConnectorConfiguration(String name, File baseFile) {
    setName(name);
    try {
      setBasePath(baseFile.getCanonicalPath());
    } catch (IOException ioe) {
      throw new RepositoryException("Unable to get canonical representation of basePath for connector " + getName() + " for baseFile " + baseFile, ioe);
    }
  }

  public FileSystemConnectorConfiguration(String basePath) {
    setBasePath(basePath);
  }
  
   
  /**
   * return configured base path for file system connector. The path is ALWAYS
   * without a trailing "/". Be aware that this may make problems on the root
   * level of the file system ("/" on Unix, "C:/" on Windows, because this leads
   * to "" and "c:", which both are considered to be relative paths
   */
  public String getBasePath() {
    baseFilePath = normalizePath(baseFilePath);
    return baseFilePath;
  }

  public void setBasePath(String basePath) {
    this.baseFilePath = normalizePath(basePath);
  }

  private String normalizePath(String path) {
    // exchange it from windows to Java sytle
    path.replace("\\", "/");
    if (path != null && path.endsWith("/")) {
      // remove trailing / to have the ids starting with a slash
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }

  @Override
  public RepositoryConnector createConnector() {
    RepositoryConnector connector = CycleComponentFactory.getCycleComponentInstance(FileSystemConnector.class, RepositoryConnector.class);
    connector.setConfiguration(this);
    return connector;
  }

}
