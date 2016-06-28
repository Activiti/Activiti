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
