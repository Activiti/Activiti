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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
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
