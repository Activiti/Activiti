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

package org.activiti.rest.builder.management;

import org.activiti.engine.runtime.Job;
import org.activiti.rest.builder.JSONConverter;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Frederik Heremans
 */
public class JobJSONConverter implements JSONConverter<Job> {

  public JSONObject getJSONObject(Job job) throws JSONException {
    JSONObject json = new JSONObject();
    JSONUtil.putRetainNull(json, "id", job.getId());
    JSONUtil.putRetainNull(json, "executionId", job.getExecutionId());
    JSONUtil.putRetainNull(json, "retries", job.getRetries());
    JSONUtil.putRetainNull(json, "processInstanceId", job.getProcessInstanceId());
    
    String dueDate = JSONUtil.formatISO8601Date(job.getDuedate());
    JSONUtil.putRetainNull(json, "dueDate", dueDate);
    JSONUtil.putRetainNull(json, "exceptionMessage", job.getExceptionMessage());
    return json;
  }

  public Job getObject(JSONObject jsonObject) throws JSONException {
    throw new UnsupportedOperationException();
  }

}
