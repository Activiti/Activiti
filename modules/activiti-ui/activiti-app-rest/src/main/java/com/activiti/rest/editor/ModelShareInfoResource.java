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
package com.activiti.rest.editor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelShareInfo;
import com.activiti.domain.idm.User;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.editor.ModelShareInfoRepresentation;
import com.activiti.model.editor.ShareInfoPermissionRepresentation;
import com.activiti.model.editor.ShareInfoUpdateRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.GroupService;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.api.UserService;
import com.activiti.service.editor.ModelShareService;

/**
 * 
 * @author Frederik Heremans
 */
@RestController
public class ModelShareInfoResource extends BaseModelResource {
    
    private static final int MAX_NUMBER_OF_SHARE_INFO = 200;
    
    @Autowired
    protected ModelShareService shareService;
    
    @Autowired
    protected UserService userService;
    
    @Autowired
    protected UserCache userCache;
    
    @Autowired
    protected GroupService groupService;

    @RequestMapping(value = "/rest/models/{modelId}/share-info",
            method = RequestMethod.GET,
            produces = "application/json")
    public ResultListDataRepresentation getProcessModelShareInfo(@PathVariable Long modelId) {
        // Only owner can view with who the process is shared
        Model model = getProcessModelForOwner(modelId);
        
        List<ModelShareInfo> shareInfoList = shareInfoRepository.findByModelIdOrderByShareDateAsc(model.getId(), new PageRequest(0, MAX_NUMBER_OF_SHARE_INFO));
        ResultListDataRepresentation result = new ResultListDataRepresentation();
        result.setSize(shareInfoList.size());
        result.setTotal(shareInfoList.size());
        result.setStart(0);
        
        if (shareInfoList.size() > 0) {
            List<ModelShareInfoRepresentation> representations = new ArrayList<ModelShareInfoRepresentation>();
            for (ModelShareInfo shareInfo : shareInfoList) {
                representations.add(new ModelShareInfoRepresentation(shareInfo, false));
            }
            result.setData(representations);
        }
        return result;
    }
    
    @RequestMapping(value = "/rest/models/{modelId}/share-info",
            method = RequestMethod.PUT,
            produces = "application/json")
    public void updateShareInfoForProcess(@PathVariable Long modelId, @RequestBody ShareInfoUpdateRepresentation update) {

        // User should be owner to alter share-info
        Model model = getProcessModelForOwner(modelId);
        
        // Handle Removals
        if (update.getRemoved() != null) {
            for (Long id : update.getRemoved()) {
                shareService.deleteShareInfo(model, id);
            }
        }
        
        // Handle updates
        if (update.getUpdated() != null) {
            ModelShareInfo shareInfo = null;
            for (ShareInfoPermissionRepresentation permission : update.getUpdated()) {
                if (permission.getId() != null) {
                    shareInfo = shareInfoRepository.findByModelIdAndId(modelId, permission.getId());
                    if (shareInfo != null) {
                        shareInfo.setPermission(permission.getSharePermission());
                        shareInfoRepository.save(shareInfo);
                    }
                }
            }
        }
        
        // Handle inserts
        if (update.getAdded() != null) {
        	
            User currentUser = SecurityUtils.getCurrentUserObject();
            for (ShareInfoPermissionRepresentation permission : update.getAdded()) {
            	
                if (permission.getUserId() != null || permission.getEmail() != null) {
                	User userToShareWith = null;
                    if (permission.getUserId() != null) {
                    	
                        // Check if this user is part of the tenant
                    	CachedUser cachedUser = userCache.getUser(permission.getUserId());

                    	userToShareWith = cachedUser.getUser();
                        
                    } else if (permission.getEmail() != null) {
                    	
                    	userToShareWith = userService.findOrCreateUserByEmail(permission.getEmail());
                    }
                    
                    shareService.shareModelWithUser(model, userToShareWith, permission.getSharePermission(), currentUser);
                }
            }
            
        }
    }
}
