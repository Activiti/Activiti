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
package org.activiti.engine.cfg;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;


/**
 * Implementations of this class can be plugged into a {@link ProcessEngineConfigurationImpl}.
 * Such implementations can configure the engine in any way programmatically possible.
 * 
 * @author Joram Barrez
 */
public interface ProcessEngineConfigurator {
  
  /**
   * Called when the engine boots up, but before it is usable.
   * Allows to tweak the process engine by passing the {@link ProcessEngineConfigurationImpl}
   * which allows tweaking it programmatically.
   */
  void configure(ProcessEngineConfigurationImpl processEngineConfiguration);
  
  /**
   * When the {@link ProcessEngineConfigurator} instances are used, they are first
   * ordered by this priority number (lowest to highest).
   * If you have dependencies between {@link ProcessEngineConfigurator}
   * instances, use the priorities accordingly to order them as needed.
   */
  int getPriority();

}
