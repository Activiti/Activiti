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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.domain.common.ImageUpload;
import com.activiti.domain.idm.User;
import com.activiti.domain.idm.UserGroup;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.repository.common.ImageUploadRepository;
import com.activiti.repository.idm.UserRepository;
import com.activiti.repository.runtime.RuntimeAppDefinitionRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.EmailConnectedModelService;
import com.activiti.service.api.UserService;
import com.activiti.service.runtime.AppConstants;

/**
 * Service class for managing users.
 *
 * @author Joram Barrez
 */
@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final int MAX_NUMBER_OF_SHARE_INFO = 200;

    @Autowired
    private Environment env;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadRepository imageUploadRepository;

    @Autowired
    private RuntimeAppDefinitionRepository runtimeAppDefinitionRepository;

    @Autowired
    private EmailConnectedModelService emailConnectedModelService;

    @Override
    public User getUser(Long userId) {
        return getUser(userId, true);
    }

    @Override
    public User getUser(Long userId, boolean initGroups) {
        User user = userRepository.findOne(userId);
        if (user != null) {
            Hibernate.initialize(user.getGroups());
        }
        return user;
    }

    @Override
    public List<User> getAllUsers(int page, int size) {
        return userRepository.findAll();
    }

    @Override
    public List<User> getRecentUsersExcludeModel(Long userId, Long modelId) {
        List<User> recentUsers = userRepository.findUsersSharedWithRecentlyExcludeModel(
                SecurityUtils.getCurrentUserId(), modelId, new PageRequest(0, MAX_NUMBER_OF_SHARE_INFO));
        return recentUsers;
    }

    /**
     * Creates a new user with the given details. Depending on the initial status, additional actions will be performed
     * (eg. send activation email).
     *
     * @throws IllegalArgumentException when a required argument is null
     * @throws IllegalStateException when a user with the given email-address is already registered
     */
    @Override
    @Transactional
    public User createNewUser(String email, String firstName, String lastName, String password, String company) {
        String encodedPassword = password != null ? passwordEncoder.encode(password) : null; // Can be null (eg with LDAP)
        return createNewUserHashedPassword(email, firstName, lastName, encodedPassword, company);
    }

    @Transactional
    public User createNewUserHashedPassword(String email, String firstName, String lastName, String password, String company) {
        return internalCreateUser(email, firstName, lastName, password, company);
    }

    private User internalCreateUser(String email, String firstName,
                                    String lastName, String password, String company) {

        try {
            if(lastName == null && firstName == null) {
                throw new IllegalArgumentException("First name or last name is required");
            }

            String lowerCasedEmail = null;
            if (email != null) {
                lowerCasedEmail = email.toLowerCase();
            }
            // Actual creation
            User user = new User();
            fillUserProperties(lowerCasedEmail, firstName, lastName, password, company, user);

            addDefaultApps(user);

            return user;
        } catch (Exception e) {
            log.error("Could not create user!", e);
        }
        return null;
    }

    private void fillUserProperties(String email, String firstName, String lastName, String password, String company, User user) {

        user.setCreated(new Date());
        user.setLastUpdate(user.getCreated());
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setCompany(company);

        userRepository.save(user);

        // Connect any models that are shared by email-address with the created user, if any
        // [Joram - 27/08/2014] Actually not needed anymore since the email -> user id refactoring
        // But kept for upgrading easily the existing Activiti Editor users
        userRepository.flush();

        if (user.getEmail() != null) {
            emailConnectedModelService.connectSharedModelsByEmail(user.getEmail(), user);
        }

    }

    protected void addDefaultApps(User user) {
        if (isReviewWorkflowsAppEnabled()) {

            List<RuntimeAppDefinition> apps = runtimeAppDefinitionRepository.findByNameIgnoreCase(AppConstants.REVIEW_WORKFLOWS_APP_NAME, new PageRequest(0, 1));
            RuntimeAppDefinition shareApp = null;
            if (CollectionUtils.isNotEmpty(apps)) {
                shareApp = apps.get(0); // in case there are multiple ... nothing more we can do than taking the first one
            }
        }
    }

    protected boolean isReviewWorkflowsAppEnabled() {
        String reviewWorkflowsAppEnabled = env.getProperty(AppConstants.REVIEW_WORKFLOWS_APP_ENABLED, "");
        return "true".equals(reviewWorkflowsAppEnabled);
    }

    /**
     * @return true, if old password matches and the password has been changed to the new one. Returns false, if the old password
     * doesn't match, the password is NOT updated.
     */
    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User currentUser = userRepository.findOne(userId);
        if (passwordEncoder.matches(oldPassword, currentUser.getPassword()) == false) {
            return false;
        }
        String newEncryptedPassword = passwordEncoder.encode(newPassword);
        currentUser.setPassword(newEncryptedPassword);
        currentUser.setLastUpdate(new Date());
        userRepository.save(currentUser);
        log.debug("Changed password for User: {}", currentUser);
        return true;
    }

    @Override
    public User findUser(long userId) {
        return userRepository.findOne(userId);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findUserByEmailFetchGroups(String email) {
        return userRepository.findByEmailFetchGroups(email);
    }

    @Override
    public User findOrCreateUserByEmail(String email) {
        User user = findUserByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            userRepository.save(user);
            userRepository.flush();
        }
        return user;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User currentUser = userRepository.findOne(userId);
        if(currentUser != null) {
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            currentUser.setLastUpdate(new Date());
            userRepository.save(currentUser);
        } else {
            throw new IllegalArgumentException("User with id: " + userId + " does not exist");
        }
    }

    @Override
    public List<User> findUsersForGroup(Long groupId, String filter, int page, int pageSize) {
        Pageable pageable = new PageRequest(page, pageSize);

        if (StringUtils.isEmpty(filter)) {
            return userRepository.findUsersForGroup(groupId, pageable);
        } else {
            return userRepository.findUsersForGroup(groupId, filter.toLowerCase(), pageable);
        }
    }

    @Override
    public Long countUsersForGroup(Long groupId, String filter) {
        if (StringUtils.isEmpty(filter)) {
            return userRepository.countUsersForGroup(groupId);
        } else {
            return userRepository.countUsersForGroup(groupId, filter.toLowerCase());
        }
    }



    @Override
    public List<User> findUsers(String filter, boolean applyFilterOnEmail,
                                String email, String company, Long groupId,
                                Pageable pageable) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);
        Root<UserGroup> userGroupRoot = null;
        if (groupId != null) {
            userGroupRoot = query.from(UserGroup.class);
        }
        query.select(userRoot);
        query.where(builder.and(getPredicatesForUserQuery(builder, userRoot, userGroupRoot, filter, applyFilterOnEmail,
                email, company, groupId).toArray(new Predicate[] {})));

        if (pageable.getSort() != null) {
            Iterator<Order> orders = pageable.getSort().iterator();
            Order order = null;
            while(orders.hasNext()) {
                order = orders.next();
                if (order.getDirection() == Direction.ASC) {
                    query.orderBy(builder.asc(userRoot.<String>get(order.getProperty())));
                } else {
                    query.orderBy(builder.desc(userRoot.<String>get(order.getProperty())));
                }
            }
        }


        TypedQuery<User> tq = em.createQuery(query);


        // Add parameters
        if (StringUtils.isNotBlank(filter)) {
            tq.setParameter("firstNameFilter", "%" + filter + "%");
            tq.setParameter("lastNameFilter", "%" + filter + "%");
            tq.setParameter("firstAndLastNameFilter", "%" + filter + "%");
            if (applyFilterOnEmail) {
                tq.setParameter("emailFilter", "%" + filter + "%");
            }
        }

        if (StringUtils.isNotBlank(email)) {
            tq.setParameter("email", email);
        }
        if (StringUtils.isNotBlank(company)) {
            tq.setParameter("company", "%" + company + "%");
        }
        if (groupId != null) {
            tq.setParameter("groupId", groupId);
        }

        tq.setFirstResult(pageable.getOffset());
        tq.setMaxResults(pageable.getPageSize());

        return tq.getResultList();
    }

    @Override
    public Long countUsers(String filter, boolean applyFilterOnEmail, String email, String company, Long groupId) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<User> userRoot = query.from(User.class);
        Root<UserGroup> userGroupRoot = null;
        if (groupId != null) {
            userGroupRoot = query.from(UserGroup.class);
        }

        query.select(builder.count(userRoot));
        query.where(builder.and(getPredicatesForUserQuery(builder, userRoot, userGroupRoot, filter, applyFilterOnEmail,
                email, company, groupId).toArray(new Predicate[] {})));

        TypedQuery<Long> tq = em.createQuery(query);


        // Add parameters
        if (StringUtils.isNotBlank(filter)) {
            tq.setParameter("firstNameFilter", "%" + filter + "%");
            tq.setParameter("lastNameFilter", "%" + filter + "%");
            tq.setParameter("firstAndLastNameFilter", "%" + filter + "%");
            if (applyFilterOnEmail) {
                tq.setParameter("emailFilter", "%" + filter + "%");
            }
        }
        if (StringUtils.isNotBlank(email)) {
            tq.setParameter("email", email);
        }
        if (StringUtils.isNotBlank(company)) {
            tq.setParameter("company", "%" + company + "%");
        }
        if (groupId != null) {
            tq.setParameter("groupId", groupId);
        }
        return tq.getSingleResult();
    }

    protected List<Predicate> getPredicatesForUserQuery( CriteriaBuilder builder, Root<User> userRoot, Root<UserGroup> userGroupRoot,
                                                         String filter, boolean applyFilterOnEmail, String email, String company, Long groupId) {

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (StringUtils.isNotBlank(filter)) {

            Predicate[] restrictions = new Predicate[applyFilterOnEmail ? 4 : 3];
            int index = 0;
            restrictions[index++] =  builder.like(builder.lower(userRoot.<String>get("firstName")),
                    builder.lower(builder.parameter(String.class, "firstNameFilter")));
            restrictions[index++] = builder.like(builder.lower(userRoot.<String>get("lastName")),
                    builder.lower(builder.parameter(String.class, "lastNameFilter")));

            if (applyFilterOnEmail) {
                restrictions[index++] = builder.like(builder.lower(userRoot.<String>get("email")),
                        builder.lower(builder.parameter(String.class, "emailFilter")));
            }

            // Search on first name and last name combined
            restrictions[index++] = builder.like(builder.lower(builder.concat(builder.concat(userRoot.<String>get("firstName"), " "), userRoot.<String>get("lastName"))),
                    builder.lower(builder.parameter(String.class, "firstAndLastNameFilter")));

            predicates.add(builder.or(restrictions));
        }

        if (groupId != null) {
            predicates.add(builder.equal(userRoot.get("id"), userGroupRoot.get("userGroupPK").get("userId")));
            predicates.add(builder.equal(userGroupRoot.get("userGroupPK").get("groupId"), builder.parameter(Long.class, "groupId")));
        }

        if (StringUtils.isNotBlank(email)) {
            predicates.add(builder.equal(userRoot.<String>get("email"), builder.parameter(String.class, "email")));
        }
        if (StringUtils.isNotBlank(company)) {
            predicates.add(builder.like(userRoot.<String>get("company"), builder.parameter(String.class, "company")));
        }

        return predicates;
    }

    @Override
    @Transactional
    public Long getUserCount() {
        return userRepository.getUserCount();
    }

    @Override
    @Transactional
    public User updateUser(Long userId, String email, String firstName, String lastName, String company) {
        User user = userRepository.findOne(userId);
        if (user == null) {
            return null;
        }

        boolean updated = false;
        if ( (user.getEmail() != null && !user.getEmail().equals(email))
                || (user.getEmail() == null && email != null) ) {

            // Check if new email is already taken
            User userWithNewEmail = userRepository.findByEmail(email);
            if (userWithNewEmail != null) {
                throw new IllegalStateException("Cannot change email address, address '" + email + "' already taken");
            }

            String originalEmail = user.getEmail() != null ? user.getEmail() : "";
            user.setEmail(email);
            updated = true;
        }

        if ( (user.getFirstName() != null && !user.getFirstName().equals(firstName))
                || (user.getFirstName() == null && firstName != null)) {
            String originalFirstName = user.getFirstName() != null ? user.getFirstName() : "";
            user.setFirstName(firstName);
            updated = true;
        }

        if ( (user.getLastName() != null && !user.getLastName().equals(lastName))
                || (user.getLastName() == null && lastName != null)) {
            String originalLastName = user.getLastName() != null ? user.getLastName() : "";
            user.setLastName(lastName);
            updated = true;
        }

        if ( (user.getCompany() != null && !user.getCompany().equals(company))
                || (user.getCompany() == null && company != null)) {
            user.setCompany(company);
            updated = true;
        }

        if (updated) {
            user.setLastUpdate(new Date());
            userRepository.save(user);
        }
        return user;
    }

    @Override
    @Transactional
    public ImageUpload updateUserPicture(MultipartFile file, Long userId) throws IOException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            return null;
        }

        ImageUpload imageUpload = new ImageUpload();
        imageUpload.setName(file.getOriginalFilename());
        imageUpload.setCreated(new Date());
        imageUpload.setUserId(SecurityUtils.getCurrentUserId());
        imageUpload.setImage(IOUtils.toByteArray(file.getInputStream()));
        imageUpload = imageUploadRepository.save(imageUpload);

        // Remove old image
        if (user.getPictureImageId() != null) {
            ImageUpload oldImage = imageUploadRepository.findOne(user.getPictureImageId());
            if (oldImage != null) {
                imageUploadRepository.delete(oldImage);
            }
        }

        // Link it to user
        user.setPictureImageId(imageUpload.getId());
        user.setLastUpdate(new Date());
        userRepository.save(user);

        return imageUpload;
    }

    @Override
    @Transactional
    public Long getUserCountByUserIdAndLastUpdateDate(Long userId, Date lastUpdate) {
        return userRepository.getUserCountByUserIdAndLastUpdateDate(userId, lastUpdate);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
