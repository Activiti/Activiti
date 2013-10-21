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

package org.activiti.rest.service.api.legacy.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.TaskQuery;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.legacy.LegacyTasksPaginateList;
import org.restlet.data.Status;
import org.restlet.resource.Get;

public class ProcessInstanceTaskResource extends SecuredResource {

	Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();

	public ProcessInstanceTaskResource() {
		properties.put("id", TaskQueryProperty.TASK_ID);
		properties.put("name", TaskQueryProperty.NAME);
		properties.put("description", TaskQueryProperty.DESCRIPTION);
		properties.put("priority", TaskQueryProperty.PRIORITY);
		properties.put("assignee", TaskQueryProperty.ASSIGNEE);
		properties.put("executionId", TaskQueryProperty.EXECUTION_ID);
		properties.put("processInstanceId", TaskQueryProperty.PROCESS_INSTANCE_ID);
	}

	@Get
	public DataResponse getTasks() {
		if (authenticate() == false)
			return null;

		String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");

		if (processInstanceId == null) {
			throw new ActivitiIllegalArgumentException("No process instance id provided");
		}

		TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery().processInstanceId(processInstanceId);

		// Return also processDefinitionName for each task
		DataResponse dataResponse = new LegacyTasksPaginateList().paginateList(getQuery(), taskQuery, "id", properties);
		return dataResponse;
	}
	
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
