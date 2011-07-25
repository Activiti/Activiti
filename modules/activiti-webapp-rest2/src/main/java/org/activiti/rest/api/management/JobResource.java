package org.activiti.rest.api.management;

import org.activiti.engine.runtime.Job;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

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
