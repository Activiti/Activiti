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
package org.activiti.rest.service.api.management;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.JobQueryProperty;
import org.activiti.engine.query.QueryProperty;

/**
 * @author Joram Barrez
 */
public class JobQueryProperties {
  
  public static Map<String, QueryProperty> PROPERTIES;

  static {
    PROPERTIES = new HashMap<String, QueryProperty>();
    PROPERTIES.put("id", JobQueryProperty.JOB_ID);
    PROPERTIES.put("dueDate", JobQueryProperty.DUEDATE);
    PROPERTIES.put("executionId", JobQueryProperty.EXECUTION_ID);
    PROPERTIES.put("processInstanceId", JobQueryProperty.PROCESS_INSTANCE_ID);
    PROPERTIES.put("retries", JobQueryProperty.RETRIES);
    PROPERTIES.put("tenantId", JobQueryProperty.TENANT_ID);
  }

}
