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

import org.activiti.engine.runtime.Job;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class JobResource extends SecuredResource {
  
  @Get
  public JobResponse getJob() {
    if(authenticate(SecuredResource.ADMIN) == false) return null;
    
    String jobId = (String) getRequest().getAttributes().get("jobId");
    Job job = ActivitiUtil.getManagementService().createJobQuery().jobId(jobId).singleResult();
    String stacktrace = ActivitiUtil.getManagementService().getJobExceptionStacktrace(jobId);
    JobResponse response = new JobResponse(job);
    response.setStacktrace(stacktrace);
    return response;
  }
}
