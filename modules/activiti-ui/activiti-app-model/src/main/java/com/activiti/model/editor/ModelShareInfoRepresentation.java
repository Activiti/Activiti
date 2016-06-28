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
