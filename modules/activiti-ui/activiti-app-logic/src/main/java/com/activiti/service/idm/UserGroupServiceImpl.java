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
package com.activiti.service.idm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.idm.UserGroup;
import com.activiti.domain.idm.UserGroup.UserGroupPK;
import com.activiti.repository.idm.UserGroupRepository;
import com.activiti.service.api.UserGroupService;

@Service
@Transactional
public class UserGroupServiceImpl implements UserGroupService {
	
	@Autowired
	private UserGroupRepository userGroupRepository;
	
	@Override
	public void save(Long userId, Long groupId) {
		UserGroupPK userGroupPK = new UserGroupPK(userId, groupId);
	    UserGroup userGroup = new UserGroup();
	    userGroup.setUserGroupPK(userGroupPK);
	    userGroupRepository.save(userGroup);
	}
	
	@Override
	public Long getCount(Long userId, Long groupId) {
		return userGroupRepository.getCount(userId, groupId);
	}
	
	@Override
	public boolean isMember(Long userId, Long groupId) {
		return getCount(userId, groupId) > 0;
	}

}
