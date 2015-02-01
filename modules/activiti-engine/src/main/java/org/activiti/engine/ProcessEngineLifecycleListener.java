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

package org.activiti.engine;

/**
 * Interface describing a listener that get's notified when certain event occurs,
 * related to the process-engine lifecycle it is attached to.
 * 
 * @author Frederik Heremans
 */
public interface ProcessEngineLifecycleListener {

  /**
   * Called right after the process-engine has been built.
   * 
   * @param processEngine engine that was built
   */
  void onProcessEngineBuilt(ProcessEngine processEngine);
  
  /**
   * Called right after the process-engine has been closed.
   * 
   * @param processEngine engine that was closed
   */
  void onProcessEngineClosed(ProcessEngine processEngine);
}
