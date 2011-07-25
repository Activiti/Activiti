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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.management.TableMetaData;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
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
