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
package org.activiti.engine.test.cfg.multitenant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.cfg.multitenant.TenantInfoHolder;
import org.activiti.engine.impl.identity.Authentication;

/**
 * @author Joram Barrez
 */
public class DummyTenantInfoHolder implements TenantInfoHolder {
  
  protected Map<String, List<String>> tenantToUserMapping = new HashMap<String, List<String>>();
  protected Map<String, String> userToTenantMapping = new HashMap<String, String>();
  
  protected ThreadLocal<String> currentUserId = new ThreadLocal<String>();
  protected ThreadLocal<String> currentTenantId = new ThreadLocal<String>();
  
  public Collection<String> getAllTenants() {
    return tenantToUserMapping.keySet();
  }
  
  public void setCurrentUserId(String userId) {
    currentUserId.set(userId);
    currentTenantId.set(userToTenantMapping.get(userId));
    
    Authentication.setAuthenticatedUserId(userId); // Activiti engine
  }
  
  public String getCurrentUserId() {
    return currentUserId.get();
  }
  
  public void clearCurrentUserId() {
    currentTenantId.set(null);
  }
  
  public void setCurrentTenantId(String tenantid) {
    currentTenantId.set(tenantid);
  }
  
  public String getCurrentTenantId() {
    return currentTenantId.get();
  }
  
 public void clearCurrentTenantId() {
   currentTenantId.set(null);
}
  
  public void addTenant(String tenantId) {
    tenantToUserMapping.put(tenantId, new ArrayList<String>());
    updateUserMap();
  }
  
  public void addUser(String tenantId, String userId) {
    tenantToUserMapping.get(tenantId).add(userId);
    updateUserMap();
  }
  
  protected void updateUserMap() {
    userToTenantMapping.clear();
    for (String tenantId : tenantToUserMapping.keySet()) {
      List<String> userIds = tenantToUserMapping.get(tenantId);
      for (String tenantUserId : userIds) {
        userToTenantMapping.put(tenantUserId, tenantId);
      }
    }
  }

}
