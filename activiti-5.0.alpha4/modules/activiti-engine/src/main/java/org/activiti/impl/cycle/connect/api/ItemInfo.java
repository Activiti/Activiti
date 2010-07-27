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
package org.activiti.impl.cycle.connect.api;

import java.util.logging.Logger;

/**
 * Superclass for the composite of folders and files. Holds a reference to the
 * API used to query sub folders and files in order to enable lazy loading of
 * them.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class ItemInfo {

  protected static Logger log = Logger.getLogger(ItemInfo.class.getName());

  protected String id;
  protected String name;
  protected String path;

  private RepositoryConnector connector;

  public ItemInfo() {
  }

  public ItemInfo(RepositoryConnector connector) {
    this.connector = connector;
  }

  public RepositoryConnector getConnector() {
    if (connector == null) {
      throw new RepositoryException("Item " + this + " is not connected to any repository");
    }
    return connector;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

}
