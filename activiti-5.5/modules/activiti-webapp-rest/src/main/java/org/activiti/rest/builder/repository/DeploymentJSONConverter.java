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

package org.activiti.rest.builder.repository;

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.builder.JSONConverter;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * @author Frederik Heremans
 */
public class DeploymentJSONConverter implements JSONConverter<Deployment> {


  public JSONObject getJSONObject(Deployment deployment) throws JSONException {
    JSONObject json = new JSONObject();
    JSONUtil.putRetainNull(json, "id", deployment.getId());
    JSONUtil.putRetainNull(json, "name", deployment.getName());
    
    String deploymentTime = JSONUtil.formatISO8601Date(deployment.getDeploymentTime());
    JSONUtil.putRetainNull(json, "deploymentTime", deploymentTime);
    
    return json;
  }

  public Deployment getObject(JSONObject jsonObject) throws JSONException {
    throw new UnsupportedOperationException();
  }

}
