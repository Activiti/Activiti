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

package org.activiti.explorer;

import org.activiti.explorer.ui.MainWindow;
import org.activiti.explorer.ui.alfresco.AlfrescoViewManager;
import org.springframework.beans.factory.FactoryBean;


/**
 * @author Joram Barrez
 */
public class ViewManagerFactoryBean implements FactoryBean<ViewManager> {

  protected String environment;
  protected MainWindow mainWindow;
  
  public ViewManager getObject() throws Exception {
    DefaultViewManager viewManagerImpl;
    if (environment.equals(Environments.ALFRESCO)) {
      viewManagerImpl = new AlfrescoViewManager();
    } else {
      viewManagerImpl = new DefaultViewManager(); 
    }
    viewManagerImpl.setMainWindow(mainWindow);
    return viewManagerImpl;
  }

  public Class<?> getObjectType() {
    return ViewManager.class;
  }

  public boolean isSingleton() {
    return true; // See https://jira.springsource.org/browse/SPR-5060
  }
  
  public void setEnvironment(String environment) {
    this.environment = environment;
  }
  
  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

}
