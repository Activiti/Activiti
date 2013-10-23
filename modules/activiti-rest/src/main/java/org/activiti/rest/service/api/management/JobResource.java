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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.Job;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestActionRequest;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author Frederik Heremans
 */
public class JobResource extends SecuredResource {

  private static final String EXECUTE_ACTION = "execute";

  @Get
  public JobResponse getJob() {
    if (authenticate() == false)
      return null;

    Job job = getJobFromResponse();

    JobResponse response = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createJobResponse(this, job);
    return response;
  }

  @Delete
  public void deleteJob() {
    if (authenticate() == false)
      return;
    
    String jobId = getAttribute("jobId");
    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("The jobId cannot be null");
    }
    try {
      ActivitiUtil.getManagementService().deleteJob(jobId);
    } catch(ActivitiObjectNotFoundException aonfe) {
      // Re-throw to have consistent error-messaging acrosse REST-api
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class); 
    }
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
  
  @Post
  public void executeJobAction(RestActionRequest actionRequest) {
    if (authenticate() == false)
      return;
    
    String jobId = getAttribute("jobId");
    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("The jobId cannot be null");
    }
    
    if(actionRequest == null || ! EXECUTE_ACTION.equals(actionRequest.getAction())) {
      throw new ActivitiIllegalArgumentException("Invalid action, only 'execute' is supported.");
    }
    
    try {
      ActivitiUtil.getManagementService().executeJob(jobId);
    } catch(ActivitiObjectNotFoundException aonfe) {
      // Re-throw to have consistent error-messaging acrosse REST-api
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class); 
    }
    
    setStatus(Status.SUCCESS_NO_CONTENT);
  }

  protected Job getJobFromResponse() {
    String jobId = getAttribute("jobId");
    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("The jobId cannot be null");
    }

    Job job = ActivitiUtil.getManagementService().createJobQuery().jobId(jobId).singleResult();

    if (job == null) {
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
    }
    return job;
  }
}
