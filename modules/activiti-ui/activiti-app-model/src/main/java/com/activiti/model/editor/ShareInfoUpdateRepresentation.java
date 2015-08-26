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

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * REST-representation containing additions, updates and removals of
 * share-info for a certain process.
 * 
 * @author Frederik Heremans
 */
public class ShareInfoUpdateRepresentation {

    protected List<Long> removed;
    protected List<ShareInfoPermissionRepresentation> added;
    protected List<ShareInfoPermissionRepresentation> updated;
    
    @JsonDeserialize(contentAs=Long.class)
    public List<Long> getRemoved() {
        return removed;
    }
    public void setRemoved(List<Long> removed) {
        this.removed = removed;
    }
    
    @JsonDeserialize(contentAs=ShareInfoPermissionRepresentation.class)
    public List<ShareInfoPermissionRepresentation> getAdded() {
        return added;
    }
    public void setAdded(List<ShareInfoPermissionRepresentation> added) {
        this.added = added;
    }
    
    @JsonDeserialize(contentAs=ShareInfoPermissionRepresentation.class)
    public List<ShareInfoPermissionRepresentation> getUpdated() {
        return updated;
    }
    
    public void setUpdated(List<ShareInfoPermissionRepresentation> updated) {
        this.updated = updated;
    }
}
