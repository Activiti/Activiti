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
