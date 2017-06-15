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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

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
 * @author Joram Barrez
 */
@RestController
@Api(tags = { "Jobs" }, description = "Manage Jobs", authorizations = { @Authorization(value = "basicAuth") })
public class JobResource {

  private static final String EXECUTE_ACTION = "execute";

  @Autowired
  protected RestResponseFactory restResponseFactory;

  @Autowired
  protected ManagementService managementService;

  @ApiOperation(value = "Get a single job", tags = {"Jobs"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the job exists and is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested job does not exist.")
  })
  @RequestMapping(value = "/management/jobs/{jobId}", method = RequestMethod.GET, produces = "application/json")
  public JobResponse getJob(@ApiParam(name = "jobId", value="The id of the job to get.") @PathVariable String jobId, HttpServletRequest request) {
    Job job = managementService.createJobQuery().jobId(jobId).singleResult();

    if (job == null) {
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
    }

    return restResponseFactory.createJobResponse(job);
  }

  @ApiOperation(value = "Get a single timer job", tags = {"Jobs"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the timer job exists and is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested job does not exist.")
  })
  @RequestMapping(value = "/management/timer-jobs/{jobId}", method = RequestMethod.GET, produces = "application/json")
  public JobResponse getTimerJob(@ApiParam(name = "jobId") @PathVariable String jobId, HttpServletRequest request) {
    Job job = managementService.createTimerJobQuery().jobId(jobId).singleResult();

    if (job == null) {
      throw new ActivitiObjectNotFoundException("Could not find a timer job with id '" + jobId + "'.", Job.class);
    }

    return restResponseFactory.createJobResponse(job);
  }

  @ApiOperation(value = "Get a single suspended job", tags = {"Jobs"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the suspended job exists and is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested job does not exist.")
  })
  @RequestMapping(value = "/management/suspended-jobs/{jobId}", method = RequestMethod.GET, produces = "application/json")
  public JobResponse getSuspendedJob(@ApiParam(name = "jobId") @PathVariable String jobId, HttpServletRequest request) {
    Job job = managementService.createSuspendedJobQuery().jobId(jobId).singleResult();

    if (job == null) {
      throw new ActivitiObjectNotFoundException("Could not find a suspended job with id '" + jobId + "'.", Job.class);
    }

    return restResponseFactory.createJobResponse(job);
  }

  @ApiOperation(value = "Get a single deadletter job", tags = {"Jobs"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the suspended job exists and is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested job does not exist.")
  })
  @RequestMapping(value = "/management/deadletter-jobs/{jobId}", method = RequestMethod.GET, produces = "application/json")
  public JobResponse getDeadletterJob(@ApiParam(name = "jobId")  @PathVariable String jobId, HttpServletRequest request) {
    Job job = managementService.createDeadLetterJobQuery().jobId(jobId).singleResult();

    if (job == null) {
      throw new ActivitiObjectNotFoundException("Could not find a deadletter job with id '" + jobId + "'.", Job.class);
    }

    return restResponseFactory.createJobResponse(job);
  }

  @ApiOperation(value = "Delete a job", tags = {"Jobs"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the job was found and has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested job was not found..")
  })
  @RequestMapping(value = "/management/jobs/{jobId}", method = RequestMethod.DELETE)
  public void deleteJob(@ApiParam(name = "jobId", value="The id of the job to delete.") @PathVariable String jobId, HttpServletResponse response) {
    try {
      managementService.deleteJob(jobId);
    } catch (ActivitiObjectNotFoundException aonfe) {
      // Re-throw to have consistent error-messaging acrosse REST-api
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  @ApiOperation(value = "Delete a timer job", tags = {"Jobs"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the job was found and has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested job was not found.")
  })
  @RequestMapping(value = "/management/timer-jobs/{jobId}", method = RequestMethod.DELETE)
  public void deleteTimerJob(@ApiParam(name = "jobId") @PathVariable String jobId, HttpServletResponse response) {
    try {
      managementService.deleteTimerJob(jobId);
    } catch (ActivitiObjectNotFoundException aonfe) {
      // Re-throw to have consistent error-messaging acrosse REST-api
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  @ApiOperation(value = "Delete a deadletter job", tags = {"Jobs"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the job was found and has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested job was not found.")
  })
  @RequestMapping(value = "/management/deadletter-jobs/{jobId}", method = RequestMethod.DELETE)
  public void deleteDeadLetterJob(@ApiParam(name = "jobId") @PathVariable String jobId, HttpServletResponse response) {
    try {
      managementService.deleteDeadLetterJob(jobId);
    } catch (ActivitiObjectNotFoundException aonfe) {
      // Re-throw to have consistent error-messaging acrosse REST-api
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  @ApiOperation(value = "Execute a single job", tags = {"Jobs"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the job was executed. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested job was not found."),
      @ApiResponse(code = 500, message = "Indicates the an exception occurred while executing the job. The status-description contains additional detail about the error. The full error-stacktrace can be fetched later on if needed.")
  })
  @RequestMapping(value = "/management/jobs/{jobId}", method = RequestMethod.POST)
  public void executeJobAction(@ApiParam(name = "jobId") @PathVariable String jobId,@ApiParam(name = "actionRequest", value="Action to perform. Only execute is supported.") @RequestBody RestActionRequest actionRequest, HttpServletResponse response) {
    if (actionRequest == null || !EXECUTE_ACTION.equals(actionRequest.getAction())) {
      throw new ActivitiIllegalArgumentException("Invalid action, only 'execute' is supported.");
    }

    try {
      managementService.executeJob(jobId);
    } catch (ActivitiObjectNotFoundException aonfe) {
      // Re-throw to have consistent error-messaging acrosse REST-api
      throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
    }

    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

}
