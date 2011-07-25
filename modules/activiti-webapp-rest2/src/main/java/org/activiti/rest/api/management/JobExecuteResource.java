package org.activiti.rest.api.management;

import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

public class JobExecuteResource extends SecuredResource {
  
  @Put
  public void executeTaskOperation(Representation entity) {
    if(authenticate(SecuredResource.ADMIN) == false) return;
    
    String jobId = (String) getRequest().getAttributes().get("jobId");
    ActivitiUtil.getManagementService().executeJob(jobId);
  }
}
