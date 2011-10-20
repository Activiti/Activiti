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

import java.util.HashMap;
import java.util.Map;

import org.activiti.explorer.ui.ComponentFactory;
import org.activiti.explorer.ui.mainlayout.MainMenuBarFactory;
import org.activiti.explorer.ui.management.ManagementMenuBarFactory;

/**
 * @author Joram Barrez
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ComponentFactories {
  
  // Component factories are configured here, and not in the Spring context,
  // as this would require a lot of XML (Alfresco doesnt use component scanning)
  public static final String MAIN_MENU = "mainMenu";
  
  // The actual factory instances
  protected Map<Class, ComponentFactory> factories = new HashMap<Class, ComponentFactory>();
  
  protected String environment;
  
  public ComponentFactories() {
    // Add custom component factories to this list
    factories.put(MainMenuBarFactory.class, new MainMenuBarFactory());
    factories.put(ManagementMenuBarFactory.class, new ManagementMenuBarFactory());
  }
  
  public <T> ComponentFactory<T> get(Class<? extends ComponentFactory<T>> clazz) {
    return factories.get(clazz);
  }
  
  public void setEnvironment(String environment) {
    this.environment = environment;
    
    // Initialise all defined component factories
    for (ComponentFactory componentFactory : factories.values()) {
      componentFactory.initialise(environment);
    }
  }
  
}
