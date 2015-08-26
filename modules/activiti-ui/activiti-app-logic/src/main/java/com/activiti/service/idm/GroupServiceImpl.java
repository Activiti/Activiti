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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.domain.idm.UserGroup;
import com.activiti.domain.idm.UserGroup.UserGroupPK;
import com.activiti.repository.idm.GroupRepository;
import com.activiti.repository.idm.UserGroupRepository;
import com.activiti.repository.idm.UserRepository;
import com.activiti.service.api.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Joram Barrez
 */
@Service
@Transactional
public class GroupServiceImpl implements GroupService {

	private static final Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);

	private static final int DB_BATCH_FETCH_SIZE = 100;

	@Autowired
	protected GroupRepository groupRepository;

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected UserGroupRepository userGroupRepository;

	@Autowired
	protected ObjectMapper objectMapper;

	@PersistenceContext
	protected EntityManager entityManager;

	@Override
	public List<Group> getGroups(String filter, int skip, int maxResults) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Group> query = builder.createQuery(Group.class);
		Root<Group> root = query.from(Group.class);

		List<Predicate> predicates = new ArrayList<Predicate>();

		// Name
		if (StringUtils.isNotBlank(filter)) {
			predicates.add(builder.like(builder.lower(root.<String>get("name")),
					builder.lower(builder.parameter(String.class, "filter"))));
		}

		// Build select with right where-clause
		query.select(root);
		query.where(builder.and(predicates.toArray(new Predicate[] {})));

		TypedQuery<Group> tq = entityManager.createQuery(query);

		// Add query parameters
		if (StringUtils.isNotBlank(filter)) {
            tq.setParameter("filter", "%" + filter + "%");
        }

		return tq.getResultList();
	}

	public Group getGroup(Long groupId) {
		return getGroup(groupId, true);
	}

    @Override
    public List<Group> getGroups() {
        return groupRepository.findAll();
    }

	public Group getGroup(Long groupId, boolean initUsers) {
		Group group = groupRepository.findOne(groupId);

		if (group != null) {
			if (initUsers) {
				Hibernate.initialize(group.getUsers());
				group.getUsers().size(); // hibernate...
			}
		}
		return group;
	}

	public Group createGroup(String groupName) {
		return internalCreateGroup(groupName);
	}

	private Group internalCreateGroup(String groupName) {

		Group group = new Group();
		group.setName(groupName);

		groupRepository.save(group);

		return group;
	}

	public Group updateGroup(Long groupId, String groupName) {
		Group group = groupRepository.findOne(groupId);
		if (group != null) {
			String originalName = group.getName();
			group.setName(groupName);
			group.setLastUpdate(new Date());
			groupRepository.save(group);
		}
		return group;
	}

	public void deleteGroup(Long groupId) {
		Group group = groupRepository.findOne(groupId);

		// Update all users (such that they can get evicted from the user cache)
		changeUsersLastUpdateValue(group);

		groupRepository.delete(group);

	}

	@Override
	public void deleteUserFromGroup(Group group, User user) {
		if (group != null && user != null) {

			// Remove membership
			userGroupRepository.deleteUserGroup(user.getId(), group.getId());

			// Update user (for cache)
			userRepository.changeLastUpdateValue(user.getId(), new Date());
		}
	}

	@Override
	public boolean addUserToGroup(Group group, User user) {

		Long groupId = group.getId();
		Long userId = user.getId();

		boolean userAlreadyInGroup = (userGroupRepository.getCount(userId, groupId) == 1);

		if (!userAlreadyInGroup) {

			// Create membership
			UserGroup userGroup = new UserGroup();
			userGroup.setUserGroupPK(new UserGroupPK(userId, groupId));
			userGroupRepository.save(userGroup);

			// Update user (for cache)
			userRepository.changeLastUpdateValue(user.getId(), new Date());

			return true;
		}

		return false;
	}

	public void changeUsersLastUpdateValue(Group group) {
		Pageable pageable = new PageRequest(0, DB_BATCH_FETCH_SIZE);
		List<Long> userIds = userRepository.findUserIdsForGroup(group.getId(), pageable);
		while (userIds.size() > 0) {
			for (Long userId : userIds) {
				userRepository.changeLastUpdateValue(userId, new Date());
			}

			pageable = pageable.next();
			userIds = userRepository.findUserIdsForGroup(group.getId(), pageable);
		}
	}

	@Override
	public Group save(Group group) {
		group.setLastUpdate(new Date());
		return groupRepository.save(group);
	}

}
