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

package org.activiti.editor.servlet.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Tijs Rademakers
 */
public class EntityManagerListener implements ServletContextListener {

  protected static final Logger LOGGER = Logger.getLogger(EntityManagerListener.class.getName());
  private static EntityManagerFactory pathwaysFactory;
  
  public void contextDestroyed(ServletContextEvent context) {
    LOGGER.log(Level.INFO, "Destroying EntityManager factory");
    pathwaysFactory.close();
  }

  public void contextInitialized(ServletContextEvent context) {
    LOGGER.log(Level.INFO, "Creating EntityManager factory");
    pathwaysFactory = Persistence.createEntityManagerFactory("org.activiti.jpa");
  }
  
  public static EntityManagerFactory getEntityManagerFactory() {
    return pathwaysFactory;
  }

}
