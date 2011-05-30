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

package org.activiti.explorer.navigation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@Component
public class NavigatorManager implements InitializingBean, Serializable {

  private static final long serialVersionUID = 1L;
  protected Map<String, Navigator> navigationHandlers = new HashMap<String, Navigator>();
  protected Navigator defaultHandler;

  public void addNavigator(Navigator handler) {
    navigationHandlers.put(handler.getTrigger(), handler);
  }

  public Navigator getNavigator(String trigger) {
    if (trigger != null) {
      return navigationHandlers.get(trigger);
    }
    return null;
  }

  public Navigator getDefaultNavigator() {
    if(defaultHandler == null) {
      throw new IllegalStateException("No default navigation handler has been set");
    }
    return defaultHandler;
  }

  public void setDefaultNavigator(Navigator handler) {
    defaultHandler = handler;
  }
  
  public void afterPropertiesSet() throws Exception {
    // Initialising all navigators
    //setDefaultNavigator(defaultHandler);
    
    addNavigator(new TaskNavigator());
    addNavigator(new ProcessNavigator());
    addNavigator(new DeploymentNavigator());
    addNavigator(new DatabaseNavigator());
    addNavigator(new JobNavigator());
    addNavigator(new UserNavigator());
    addNavigator(new GroupNavigator());
    addNavigator(new MyProcessesNavigator());
  }

}
