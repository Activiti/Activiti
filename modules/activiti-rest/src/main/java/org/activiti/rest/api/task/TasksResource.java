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

package org.activiti.rest.api.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.TaskQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.identity.GroupInfo;
import org.activiti.rest.api.identity.UserGroupsPaginateList;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class TasksResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public TasksResource() {
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
    if(authenticate() == false) return null;
    
    String personalTaskUserId = getQuery().getValues("assignee");
    String ownerTaskUserId = getQuery().getValues("owner");
    String involvedTaskUserId = getQuery().getValues("involved");
    String candidateTaskUserId = getQuery().getValues("candidate");
    String candidateGroupId = getQuery().getValues("candidate-group");
    String candidateGroupIds = getQuery().getValues("candidate-groups");
    String candidateGroupUserId = getQuery().getValues("candidate-group-user-id");
    
    String strPriority = getQuery().getValues("priority");
    String strMinPriority = getQuery().getValues("minPriority");
    String strMaxPriority = getQuery().getValues("maxPriority");
    
    String strDueDate = getQuery().getValues("dueDate");
    String strMinDueDate = getQuery().getValues("minDueDate");
    String strMaxDueDate = getQuery().getValues("maxDueDate");
    
    TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery();
    if (personalTaskUserId != null) {
      taskQuery.taskAssignee(personalTaskUserId);
    } else if (ownerTaskUserId != null) {
      taskQuery.taskOwner(ownerTaskUserId);
    } else if (involvedTaskUserId != null) {
      taskQuery.taskInvolvedUser(involvedTaskUserId);
    } else if (candidateTaskUserId != null) {
      taskQuery.taskCandidateUser(candidateTaskUserId);
    } else if (candidateGroupId != null) {
      taskQuery.taskCandidateGroup(candidateGroupId);
    }else if (candidateGroupIds != null){
    	taskQuery.taskCandidateGroupIn(Arrays.asList(candidateGroupIds.split("\\|")));
    }else if (candidateGroupUserId != null){
    	DataResponse userGroupsDataResponse = new UserGroupsPaginateList().paginateList(
    	        getQuery(), ActivitiUtil.getIdentityService().createGroupQuery()
    	            .groupMember(candidateGroupUserId), "id", properties);
    	ArrayList<GroupInfo> userGroupsInfo = (userGroupsDataResponse.getData() instanceof ArrayList<?>) ? (ArrayList<GroupInfo>)userGroupsDataResponse.getData() : null;
    	
    	if(userGroupsInfo != null && userGroupsInfo.size() > 0){
	    	List<String> candidateGroups = new ArrayList<String>();
	    	for(GroupInfo groupInfo : userGroupsInfo){
	    		candidateGroups.add(groupInfo.getId());
	    	}
	    	
	    	taskQuery.taskCandidateGroupIn(candidateGroups);
    	}
    	else
    	{
    		throw new ActivitiException("User Id " + candidateGroupUserId + " does not exist, or there are no groups associated with it.");
    	}
    }else {
      throw new ActivitiIllegalArgumentException("Tasks must be filtered with 'assignee', 'owner', 'involved', 'candidate' or 'candidate-group'");
    }
        
    if (strPriority != null) {
      taskQuery.taskPriority(RequestUtil.parseToInteger(strPriority));
    } else if (strMinPriority != null) {
      taskQuery.taskMinPriority(RequestUtil.parseToInteger(strMinPriority));
    } else if (strMaxPriority != null) {
      taskQuery.taskMaxPriority(RequestUtil.parseToInteger(strMaxPriority));
    }
    
    if (strDueDate != null) {
      taskQuery.dueDate(RequestUtil.parseToDate(strDueDate));
    } else if (strMinDueDate != null) {
      taskQuery.dueAfter(RequestUtil.parseToDate(strMinDueDate));
    } else if (strMaxDueDate != null) {
      taskQuery.dueBefore(RequestUtil.parseToDate(strMaxDueDate));
    }
    
    DataResponse dataResponse = new TasksPaginateList().paginateList(getQuery(), taskQuery, "id", properties);
    return dataResponse;
  }
}
