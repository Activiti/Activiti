package org.activiti.rest.api.management;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.management.TableMetaData;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class TableResource extends SecuredResource {
  
  @Get
  public TableMetaData getTableMetaData() {
    if(authenticate(SecuredResource.ADMIN) == false) return null;
    
    String tableName = (String) getRequest().getAttributes().get("tableName");
    if(tableName == null) {
      throw new ActivitiException("table name is required");
    }
    return ActivitiUtil.getManagementService().getTableMetaData(tableName);
  }
}
