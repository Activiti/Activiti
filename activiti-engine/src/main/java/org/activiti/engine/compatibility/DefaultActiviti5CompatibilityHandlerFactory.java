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
package org.activiti.engine.compatibility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class DefaultActiviti5CompatibilityHandlerFactory implements Activiti5CompatibilityHandlerFactory {

  private static final Logger logger = LoggerFactory.getLogger(DefaultActiviti5CompatibilityHandlerFactory.class);

  protected String compatibilityHandlerClassName;

  @Override
  public Activiti5CompatibilityHandler createActiviti5CompatibilityHandler() {

    if (compatibilityHandlerClassName == null) {
      compatibilityHandlerClassName = "org.activiti.compatibility.DefaultActiviti5CompatibilityHandler";
    }

    try {
      Activiti5CompatibilityHandler handler = (Activiti5CompatibilityHandler) Class.forName(compatibilityHandlerClassName).newInstance();
      return handler;
    } catch (Exception e) {
      logger.info("Activiti 5 compatibility handler implementation not found or error during instantiation : " + e.getMessage() + ". Activiti 5 backwards compatibility disabled.");
    }
    return null;
  }

  public String getCompatibilityHandlerClassName() {
    return compatibilityHandlerClassName;
  }

  public void setCompatibilityHandlerClassName(String compatibilityHandlerClassName) {
    this.compatibilityHandlerClassName = compatibilityHandlerClassName;
  }

}
