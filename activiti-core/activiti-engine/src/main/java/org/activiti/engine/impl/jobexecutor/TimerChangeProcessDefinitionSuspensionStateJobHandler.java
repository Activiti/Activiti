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
package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.util.json.JSONObject;

/**

 */
public abstract class TimerChangeProcessDefinitionSuspensionStateJobHandler implements JobHandler {

  private static final String JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES = "includeProcessInstances";

  public static String createJobHandlerConfiguration(boolean includeProcessInstances) {
    JSONObject json = new JSONObject();
    json.put(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES, includeProcessInstances);
    return json.toString();
  }

  public static boolean getIncludeProcessInstances(JSONObject jobHandlerCfgJson) {
    return jobHandlerCfgJson.getBoolean(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES);
  }

}
