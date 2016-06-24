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
