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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.runtime.Job;
import org.activiti.rest.service.api.RestActionRequest;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class JobResource {

  private static final String EXECUTE_ACTION = "execute";
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected ManagementService managementService;

  @RequestMapping(value="/management/jobs/{jobId}", method = RequestMethod.GET, produces = "application/json")
  public JobResponse getJob(@PathVariable String jobId, HttpServletRequest request) {
    Job job = getJobFromResponse(jobId);
    
    return restResponseFactory.createJobResponse(job);
  }

  @RequestMapping(value="/management/jobs/{jobId}", method = RequestMethod.DELETE)
  public void deleteJob(@PathVariable String jobId, HttpServletResponse response) {
    try {
      managementService.deleteJob(jobId);
    } catch(ActivitiObjectNotFoundException aonfe) {
      // Re-throw to have consistent error-messaging acrosse REST-api
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class); 
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
  
  @RequestMapping(value="/management/jobs/{jobId}", method = RequestMethod.POST)
  public void executeJobAction(@PathVariable String jobId, @RequestBody RestActionRequest actionRequest, HttpServletResponse response) { 
    if (actionRequest == null || ! EXECUTE_ACTION.equals(actionRequest.getAction())) {
      throw new ActivitiIllegalArgumentException("Invalid action, only 'execute' is supported.");
    }
    
    try {
      managementService.executeJob(jobId);
    } catch(ActivitiObjectNotFoundException aonfe) {
      // Re-throw to have consistent error-messaging acrosse REST-api
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class); 
    }
    
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  protected Job getJobFromResponse(String jobId) {
    Job job = managementService.createJobQuery().jobId(jobId).singleResult();

    if (job == null) {
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
    }
    return job;
  }
}
