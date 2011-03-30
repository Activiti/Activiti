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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frederik Heremans
 */
public class NavigationHandlers {

  protected static Map<String, NavigationHandler> navigationHandlers = new HashMap<String, NavigationHandler>();
  protected static NavigationHandler defaultHandler;

  public static void addNavigationHandler(NavigationHandler handler) {
    navigationHandlers.put(handler.getTrigger(), handler);
  }

  public static NavigationHandler getHandler(String trigger) {
    if (trigger != null) {
      return navigationHandlers.get(trigger);
    }
    return null;
  }

  public static NavigationHandler getDefaultHandler() {
    if(defaultHandler == null) {
      throw new IllegalStateException("No default navigation handler has been set");
    }
    return defaultHandler;
  }

  public static void setDefaultHandler(NavigationHandler handler) {
    defaultHandler = handler;
  }

}
