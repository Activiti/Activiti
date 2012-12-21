package org.activiti.rest.api.history;

import java.util.List;

import org.activiti.engine.history.*;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;


/**
 * @author Franco Lombardo
 */
public class HistoricFormPropertiesResource extends SecuredResource {

  @Get
  public ObjectNode getHistoricFormProperties() {
    if(authenticate() == false) return null;
    
    String taskId = (String) getRequest().getAttributes().get("taskId");
    
    HistoricDetailQuery createHistoricDetailQuery = 
        ActivitiUtil.getHistoryService().createHistoricDetailQuery();
    
    List<HistoricDetail> list = 
        createHistoricDetailQuery.taskId(taskId).formProperties().list();
    
    ObjectNode responseJSON = new ObjectMapper().createObjectNode();
    ArrayNode propertiesJSON = new ObjectMapper().createArrayNode();
    
    if(list != null) {
      for (HistoricDetail historicDetail : list) {
        HistoricFormProperty property = (HistoricFormProperty)historicDetail;
        ObjectNode propertyJSON = new ObjectMapper().createObjectNode();
        propertyJSON.put("id", property.getPropertyId());
        propertyJSON.put("value", property.getPropertyValue());
        propertiesJSON.add(propertyJSON);
      }
    }
  
    responseJSON.put("data", propertiesJSON);
    
    return responseJSON;
  }
  
}
