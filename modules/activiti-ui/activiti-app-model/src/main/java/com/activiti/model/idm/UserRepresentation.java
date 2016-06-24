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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.model.editor.LightAppRepresentation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class UserRepresentation extends AbstractUserRepresentation {

    protected String fullname;
    protected String password;
    protected String company;
    protected Date created;
    protected Date lastUpdate;
    protected List<GroupRepresentation> groups;
    protected List<String> capabilities;
    protected List<LightAppRepresentation> apps = new ArrayList<LightAppRepresentation>();
    
	public UserRepresentation(User user) {
		this(user, false, false);
	}
    
	public UserRepresentation(User user, boolean fullDetails, boolean includeGroups) {
	    super(user);
		this.created = user.getCreated();
		this.lastUpdate = user.getLastUpdate();
		this.fullname = user.getFullName();
		this.company = user.getCompany();

		if (fullDetails) {
			this.created = user.getCreated();
		}

		if (includeGroups) {
			if (user.getGroups() != null) {
				this.groups = new ArrayList<GroupRepresentation>(user.getGroups().size());
				for (Group group : user.getGroups()) {
					this.groups.add(new GroupRepresentation(group, false));
				}
			}

		}
	}
    
    public UserRepresentation() {
        
    }
    
    public String getFullname() {
        return fullname;
    }
    
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @JsonInclude(Include.NON_NULL)
    public Date getCreated() {
        return created;
    }
    
    @JsonInclude(Include.NON_NULL)
	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	public List<GroupRepresentation> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupRepresentation> groups) {
		this.groups = groups;
	}

	public List<String> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<String> capabilities) {
		this.capabilities = capabilities;
	}

    public List<LightAppRepresentation> getApps() {
        return apps;
    }

    public void setApps(List<LightAppRepresentation> apps) {
        this.apps = apps;
    }
}
