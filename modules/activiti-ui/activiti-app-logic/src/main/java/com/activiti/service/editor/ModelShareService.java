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
