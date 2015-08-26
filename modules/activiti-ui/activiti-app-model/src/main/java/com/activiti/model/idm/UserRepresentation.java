/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
