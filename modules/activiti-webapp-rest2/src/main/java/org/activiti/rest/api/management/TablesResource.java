package org.activiti.rest.api.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

public class TablesResource extends SecuredResource {
  
  @Get
  public ObjectNode getTables() {
    if(authenticate(SecuredResource.ADMIN) == false) return null;
    
    Map<String, Long> tableCounts = ActivitiUtil.getManagementService().getTableCount();
    ArrayList<String> tableNames = new ArrayList<String>(tableCounts.keySet());
    Collections.sort(tableNames);
    
    ObjectNode responseJSON = new ObjectMapper().createObjectNode();
    
    ArrayNode tablesJSON = new ObjectMapper().createArrayNode();
    responseJSON.put("data", tablesJSON);
    
    for (String tableName : tableNames) {
      ObjectNode tableJSON = new ObjectMapper().createObjectNode();
      tableJSON.put("tableName", tableName);
      tableJSON.put("total", tableCounts.get(tableName));
      tablesJSON.add(tableJSON);
    }
    
    return responseJSON;
  }
}
