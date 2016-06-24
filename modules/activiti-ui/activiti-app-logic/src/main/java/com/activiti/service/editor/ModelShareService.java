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
package com.activiti.service.editor;

import java.util.Calendar;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelShareInfo;
import com.activiti.domain.editor.SharePermission;
import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.repository.editor.ModelShareInfoRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserService;

/**
 * Service to add, update and remove sharing information.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@Service
@Transactional
public class ModelShareService {
    
    @Inject 
    protected ModelShareInfoRepository shareInfoRepository;
    
    @Inject
    protected UserService userService;
    
    /**
     * Share the given model with the given group. In case the model is already shared with that group,
     * the permission is updated.
     * 
     * @return The share info that is created or updated. Null, if the given group does not exist
     */
    public ModelShareInfo shareModelWithGroup(Model model, Group groupToShareWith, SharePermission permission) {
        // Check if there is already a share-info for this group
        ModelShareInfo shareInfo = shareInfoRepository.findByModelIdAndGroupId(model.getId(), groupToShareWith.getId());
        if (shareInfo == null) {
            
            User currentUser = SecurityUtils.getCurrentUserObject();
            shareInfo = new ModelShareInfo();
            shareInfo.setSharedBy(currentUser);
            shareInfo.setShareDate(Calendar.getInstance().getTime());
            shareInfo.setGroup(groupToShareWith);
            shareInfo.setModel(model);
        }
        
        if (shareInfo != null) {
            shareInfo.setPermission(permission);
            shareInfoRepository.save(shareInfo);
        }
        
        return shareInfo;
    }
    
    /**
     * Share the given model with the given userLogin. In case the model is already shared with that user,
     * the permission is updated.
     * 
     * @return The share info that is created or updated. Null, if the given user does not exist
     */
    public ModelShareInfo shareModelWithUser(Model model, User userToShareWith, SharePermission permission, User sharedBy) {
        // Check if there is already a share-info for this user
        ModelShareInfo shareInfo = shareInfoRepository.findByModelIdAndUserId(model.getId(), userToShareWith.getId());
        if(shareInfo == null) {
            
            // Only create shareInfo when user actually exists
            if (userToShareWith != null) {
                shareInfo = new ModelShareInfo();
                shareInfo.setSharedBy(sharedBy);
                shareInfo.setShareDate(Calendar.getInstance().getTime());
                shareInfo.setUser(userToShareWith);
                shareInfo.setModel(model);
            }
        }
        
        if (shareInfo != null) {
            shareInfo.setPermission(permission);
            shareInfoRepository.save(shareInfo);
        }
        
        return shareInfo;
    }
    
    /**
     * @return true, if the share info was updated. False, if no share-info exists with the given id.
     */
    public boolean updateShareInfo(Model model, String shareInfoId, SharePermission permission) {
        ModelShareInfo shareInfo = shareInfoRepository.findByModelIdAndId(model.getId(), shareInfoId);
        if(shareInfo != null) {
            shareInfo.setPermission(permission);
            shareInfoRepository.save(shareInfo);
            return true;
        }
        return false;
    }
    
    public boolean deleteShareInfo(Model model, Long shareInfoId) {
        ModelShareInfo shareInfo = shareInfoRepository.findByModelIdAndId(model.getId(), shareInfoId);
        if(shareInfo != null) {
            shareInfoRepository.delete(shareInfo);
            return true;
        }
        return false;
    }

}
