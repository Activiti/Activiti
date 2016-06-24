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
package com.activiti.model.idm;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class BulkUserUpdateRepresentation {

    private String status;
    private String accountType;
    private String password;
    private Long tenantId;
    private boolean sendNotifications = true;
    
    private List<Long> users;
    
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    public String getAccountType() {
        return accountType;
    }
    
    public void setUsers(List<Long> users) {
        this.users = users;
    }
    
    @JsonDeserialize(contentAs=Long.class)
    public List<Long> getUsers() {
        return users;
    }
    
    public void setSendNotifications(boolean sendNotifictions) {
        this.sendNotifications = sendNotifictions;
    }
    
    public boolean isSendNotifications() {
        return sendNotifications;
    }
    
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	public Long getTenantId() {
		return tenantId;
	}
	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}
	
}
