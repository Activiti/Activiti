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

package org.activiti.rest.api.management;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.JobQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.data.Form;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class JobCollectionResource extends SecuredResource {

  protected static Map<String, QueryProperty> properties;

  static {
    properties = new HashMap<String, QueryProperty>();
    properties.put("id", JobQueryProperty.JOB_ID);
    properties.put("dueDate", JobQueryProperty.DUEDATE);
    properties.put("executionId", JobQueryProperty.EXECUTION_ID);
    properties.put("processInstanceId", JobQueryProperty.PROCESS_INSTANCE_ID);
    properties.put("retries", JobQueryProperty.RETRIES);
  }
  
  @Get
  public DataResponse getJobs() {
    if (authenticate() == false)
      return null;
    
    JobQuery query = ActivitiUtil.getManagementService().createJobQuery();
    Form form = getQuery();
    
    if(form.getNames().contains("id")) {
      query.jobId(getQueryParameter("id", form));
    }
    if(form.getNames().contains("processInstanceId")) {
      query.processInstanceId(getQueryParameter("processInstanceId", form));
    }
    if(form.getNames().contains("executionId")) {
      query.executionId(getQueryParameter("executionId", form));
    }
    if(form.getNames().contains("processDefinitionId")) {
      query.processDefinitionId(getQueryParameter("processDefinitionId", form));
    }
    if(form.getNames().contains("withRetriesLeft")) {
      if(Boolean.TRUE.equals(getQueryParameterAsBoolean("withRetriesLeft", form))) {
        query.withRetriesLeft();
      }
    }
    if(form.getNames().contains("executable")) {
      if(Boolean.TRUE.equals(getQueryParameterAsBoolean("executable", form))) {
        query.executable();
      }
    }
    if(form.getNames().contains("timersOnly")) {
      if(form.getNames().contains("messagesOnly")) {
        throw new ActivitiIllegalArgumentException("Only one of 'timersOnly' or 'messagesOnly' can be provided.");
      }
      if(Boolean.TRUE.equals(getQueryParameterAsBoolean("timersOnly", form))) {
        query.timers();
      }
    }
    if(form.getNames().contains("messagesOnly")) {
      if(Boolean.TRUE.equals(getQueryParameterAsBoolean("messagesOnly", form))) {
        query.messages();
      }
    }
    if(form.getNames().contains("dueBefore")) {
      query.duedateLowerThan(getQueryParameterAsDate("dueBefore", form));
    }
    if(form.getNames().contains("dueAfter")) {
      query.duedateHigherThan(getQueryParameterAsDate("dueAfter", form));
    }
    if(form.getNames().contains("withException")) {
      if(Boolean.TRUE.equals(getQueryParameterAsBoolean("withException", form))) {
        query.withException();
      }
    }
    if(form.getNames().contains("exceptionMessage")) {
      query.exceptionMessage(getQueryParameter("exceptionMessage", form));
    }

    return new JobPaginateList(this).paginateList(form, query, "id", properties);
  }
}
