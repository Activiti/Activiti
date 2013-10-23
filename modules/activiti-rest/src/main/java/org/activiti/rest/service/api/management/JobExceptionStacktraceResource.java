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

import java.io.ByteArrayInputStream;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.Job;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class JobExceptionStacktraceResource extends SecuredResource {

  @Get
  public InputRepresentation getJobStacktrace() {
    if (authenticate() == false)
      return null;

    Job job = getJobFromResponse();
    
    String stackTrace = ActivitiUtil.getManagementService().getJobExceptionStacktrace(job.getId());
    
    if(stackTrace == null) {
      throw new ActivitiObjectNotFoundException("Job with id '" + job.getId() + "' doesn't have an exception stacktrace.", String.class);
    }
    
    return new InputRepresentation(new ByteArrayInputStream(stackTrace.getBytes()), MediaType.TEXT_PLAIN);
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
