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
package com.activiti.model.editor;

import java.util.Date;

import com.activiti.domain.editor.ModelShareInfo;
import com.activiti.domain.editor.SharePermission;
import com.activiti.model.common.AbstractRepresentation;
import com.activiti.model.idm.LightGroupRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Frederik Heremans
 */
public class ModelShareInfoRepresentation extends AbstractRepresentation {
    
    protected Long id;
    protected String permission;
    protected Date shareDate;
    protected Long sharedBy;
    
    protected LightUserRepresentation person;
    protected LightGroupRepresentation group;
    
    protected Long processId;
    protected String processName;
    
    public ModelShareInfoRepresentation(ModelShareInfo shareInfo, boolean includeProcessModel) {
        this.id = shareInfo.getId();
        this.shareDate = shareInfo.getShareDate();
        this.permission = shareInfo.getPermission().toString().toLowerCase();
        if (shareInfo.getSharedBy() != null) {
            this.sharedBy = shareInfo.getSharedBy().getId();
        }
        
        if (shareInfo.getEmail() != null || shareInfo.getUser() != null) {
            this.person = new LightUserRepresentation();
            this.person.setEmail(shareInfo.getEmail());
        	this.person.setId(shareInfo.getUser().getId());
        	this.person.setFirstName(shareInfo.getUser().getFirstName());
        	this.person.setLastName(shareInfo.getUser().getLastName());
        }
        
        if (shareInfo.getGroup() != null) {
            this.group = new LightGroupRepresentation(shareInfo.getGroup());
        }
        
        if (includeProcessModel) {
            this.processId = shareInfo.getModel().getId();
            this.processName = shareInfo.getModel().getName();
        }
    }

    @JsonInclude(Include.NON_NULL)
    public Long getProcessId() {
        return processId;
    }
    public void setProcessId(Long processId) {
        this.processId = processId;
    }
    public String getPermission() {
        return permission;
    }
    public void setPermission(String permission) {
        this.permission = permission;
    }
    public Date getShareDate() {
        return shareDate;
    }
    public void setShareDate(Date shareDate) {
        this.shareDate = shareDate;
    }
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    @JsonInclude(Include.NON_NULL)
    public String getProcessName() {
        return processName;
    }

	public LightUserRepresentation getPerson() {
		return person;
	}

	public void setPerson(LightUserRepresentation person) {
		this.person = person;
	}
	

	public LightGroupRepresentation getGroup() {
        return group;
    }

    public void setGroup(LightGroupRepresentation group) {
        this.group = group;
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public Long getSharedBy() {
        return sharedBy;
    }
    
    public void setSharedBy(Long sharedBy) {
        this.sharedBy = sharedBy;
    }
    
    /**
     * @return a {@link SharePermission} value, based on the string-represention in this object. Will
     * return <code>null</code> if empty or in case an unknown value is set.
     */
    @JsonIgnore
    public SharePermission getSharePermission() {
        if(this.permission != null) {
            for(SharePermission p : SharePermission.values()) {
                if(p.name().toLowerCase().equals(this.permission)) {
                    return p;
                }
            }
        }
        return null;
    }
}
