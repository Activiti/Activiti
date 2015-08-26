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

import com.activiti.domain.editor.SharePermission;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ShareInfoPermissionRepresentation {

    protected Long id;
    protected Long userId;
    protected String email;
    protected String permission;
    
    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * @return a {@link SharePermission} value, based on the string-represention in this object. Will
     * return <code>null</code> if empty or in case an unknown value is set.
     */
    @JsonIgnore
    public SharePermission getSharePermission() {
        if (this.permission != null) {
            for (SharePermission p : SharePermission.values()) {
                if (p.name().toLowerCase().equals(this.permission)) {
                    return p;
                }
            }
        }
        return null;
    }
}
