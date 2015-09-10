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
package org.activiti.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityImpl;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link UserEntityManager} interface specifically for LDAP.
 * 
 * Note that only a few methods are actually implemented, as many of the operations 
 * (save, update, etc.) are done on the LDAP system directly. 
 * 
 * @author Joram Barrez
 */
public class LDAPUserManager extends AbstractManager implements UserEntityManager {

  private static Logger logger = LoggerFactory.getLogger(LDAPUserManager.class);

  protected LDAPConfigurator ldapConfigurator;

  public LDAPUserManager(ProcessEngineConfigurationImpl processEngineConfiguration, LDAPConfigurator ldapConfigurator) {
    super(processEngineConfiguration);
    this.ldapConfigurator = ldapConfigurator;
  }

  @Override
  public User createNewUser(String userId) {
    throw new ActivitiException("LDAP user manager doesn't support creating a new user");
  }
  
  @Override
  public UserEntity create() {
    throw new ActivitiException("LDAP user manager doesn't support creating a new user");
  }

  @Override
  public void updateUser(User updatedUser) {
    throw new ActivitiException("LDAP user manager doesn't support updating a user");
  }
  
  @Override
  public UserEntity update(UserEntity entity) {
    throw new ActivitiException("LDAP user manager doesn't support updating a user");
  }
  
  @Override
  public UserEntity update(UserEntity entity, boolean fireUpdateEvent) {
    throw new ActivitiException("LDAP user manager doesn't support updating a user");
  }

  @Override
  public boolean isNewUser(User user) {
    throw new ActivitiException("LDAP user manager doesn't support adding or updating a user");
  }

  @Override
  public UserEntity findById(final String userId) {
    LDAPTemplate ldapTemplate = new LDAPTemplate(ldapConfigurator);
    return ldapTemplate.execute(new LDAPCallBack<UserEntity>() {

      public UserEntity executeInContext(InitialDirContext initialDirContext) {
        try {

          String searchExpression = ldapConfigurator.getLdapQueryBuilder().buildQueryByUserId(ldapConfigurator, userId);

          String baseDn = ldapConfigurator.getUserBaseDn() != null ? ldapConfigurator.getUserBaseDn() : ldapConfigurator.getBaseDn();
          NamingEnumeration<?> namingEnum = initialDirContext.search(baseDn, searchExpression, createSearchControls());
          UserEntity user = new UserEntityImpl();
          while (namingEnum.hasMore()) { // Should be only one
            SearchResult result = (SearchResult) namingEnum.next();
            mapSearchResultToUser(result, user);
          }
          namingEnum.close();

          return user;

        } catch (NamingException ne) {
          logger.debug("Could not find user " + userId + " : " + ne.getMessage(), ne);
          return null;
        }
      }

    });
  }

  @Override
  public void deletePicture(User user) {
    throw new ActivitiException("LDAP user manager doesn't support deleting a user picture");
  }

  @Override
  public List<User> findUserByQueryCriteria(final UserQueryImpl query, final Page page) {

    if (query.getId() != null) {
      List<User> result = new ArrayList<User>();
      result.add(findById(query.getId()));
      return result;
    } else if (query.getFullNameLike() != null){
      
      final String fullNameLike = query.getFullNameLike().replaceAll("%", "");
      
      LDAPTemplate ldapTemplate = new LDAPTemplate(ldapConfigurator);
      return ldapTemplate.execute(new LDAPCallBack<List<User>>() {

        public List<User> executeInContext(InitialDirContext initialDirContext) {
          List<User> result = new ArrayList<User>();
          try {
            String searchExpression = ldapConfigurator.getLdapQueryBuilder().buildQueryByFullNameLike(ldapConfigurator, fullNameLike);
            String baseDn = ldapConfigurator.getUserBaseDn() != null ? ldapConfigurator.getUserBaseDn() : ldapConfigurator.getBaseDn();
            NamingEnumeration<?> namingEnum = initialDirContext.search(baseDn, searchExpression, createSearchControls());

            while (namingEnum.hasMore()) {
              SearchResult searchResult = (SearchResult) namingEnum.next();

              UserEntity user = new UserEntityImpl();
              mapSearchResultToUser(searchResult, user);
              result.add(user);

            }
            namingEnum.close();

          } catch (NamingException ne) {
            logger.debug("Could not execute LDAP query: " + ne.getMessage(), ne);
            return null;
          }
          return result;
        }

      });

    } else {
      throw new ActivitiIllegalArgumentException("Query is currently not supported by LDAPUserManager.");
    }

  }

  protected void mapSearchResultToUser(SearchResult result, UserEntity user) throws NamingException {
    if (ldapConfigurator.getUserIdAttribute() != null) {
      user.setId(result.getAttributes().get(ldapConfigurator.getUserIdAttribute()).get().toString());
    }
    if (ldapConfigurator.getUserFirstNameAttribute() != null) {
      try {
        user.setFirstName(result.getAttributes().get(ldapConfigurator.getUserFirstNameAttribute()).get().toString());
      } catch (NullPointerException e) {
        user.setFirstName("");
      }
    }
    if (ldapConfigurator.getUserLastNameAttribute() != null) {
      try {
        user.setLastName(result.getAttributes().get(ldapConfigurator.getUserLastNameAttribute()).get().toString());
      } catch (NullPointerException e) {
        user.setLastName("");
      }
    }
    if (ldapConfigurator.getUserEmailAttribute() != null) {
        try {
            user.setEmail(result.getAttributes().get(ldapConfigurator.getUserEmailAttribute()).get().toString());
        }catch(NullPointerException e){
    		user.setEmail("");
    	}
    }
  }

  @Override
  public long findUserCountByQueryCriteria(UserQueryImpl query) {
    return findUserByQueryCriteria(query, null).size(); // Is there a
                                                        // generic way to do
                                                        // counts in ldap?
  }

  @Override
  public List<Group> findGroupsByUser(String userId) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }

  @Override
  public UserQuery createNewUserQuery() {
    return new UserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutor());
  }

  @Override
  public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }

  @Override
  public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }

  @Override
  public void setUserPicture(String userId, Picture picture) {
    throw new ActivitiException("LDAP user manager doesn't support user pictures");
  }

  @Override
  public Picture getUserPicture(String userId) {
    logger.debug("LDAP user manager doesn't support user pictures. Returning null");
    return null;
  }

  @Override
  public Boolean checkPassword(final String userId, final String password) {

    // Extra password check, see http://forums.activiti.org/comment/22312
    if (password == null || password.length() == 0) {
      throw new ActivitiException("Null or empty passwords are not allowed!");
    }

    try {
      LDAPTemplate ldapTemplate = new LDAPTemplate(ldapConfigurator);
      return ldapTemplate.execute(new LDAPCallBack<Boolean>() {

        public Boolean executeInContext(InitialDirContext initialDirContext) {

          if (initialDirContext == null) {
            return false;
          }

          // Do the actual search for the user
          String userDn = null;
          try {

            String searchExpression = ldapConfigurator.getLdapQueryBuilder().buildQueryByUserId(ldapConfigurator, userId);
            String baseDn = ldapConfigurator.getUserBaseDn() != null ? ldapConfigurator.getUserBaseDn() : ldapConfigurator.getBaseDn();
            NamingEnumeration<?> namingEnum = initialDirContext.search(baseDn, searchExpression, createSearchControls());

            while (namingEnum.hasMore()) { // Should be only one
              SearchResult result = (SearchResult) namingEnum.next();
              userDn = result.getNameInNamespace();
            }
            namingEnum.close();

          } catch (NamingException ne) {
            logger.info("Could not authenticate user " + userId + " : " + ne.getMessage(), ne);
            return false;
          }

          // Now we have the user DN, we can need to create a
          // connection it
          // ('bind' in ldap lingo)
          // to check if the user is valid
          if (userDn != null) {
            InitialDirContext verificationContext = null;
            try {
              verificationContext = LDAPConnectionUtil.createDirectoryContext(ldapConfigurator, userDn, password);
            } catch (ActivitiException e) {
              // Do nothing, an exception will be thrown if the
              // login fails
            }

            if (verificationContext != null) {
              LDAPConnectionUtil.closeDirectoryContext(verificationContext);
              return true;
            }
          }

          return false;

        }
      });

    } catch (ActivitiException e) {
      logger.info("Could not authenticate user : " + e);
      return false;
    }
  }

  protected SearchControls createSearchControls() {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setTimeLimit(ldapConfigurator.getSearchTimeLimit());
    return searchControls;
  }

  @Override
  public void insert(UserEntity entity) {
    throw new ActivitiException("Unsupported by LDAP user manager");    
  }

  @Override
  public void insert(UserEntity entity, boolean fireCreateEvent) {
    throw new ActivitiException("Unsupported by LDAP user manager");
  }

  @Override
  public void delete(String id) {
    throw new ActivitiException("Unsupported by LDAP user manager");
  }

  @Override
  public void delete(UserEntity entity) {
    throw new ActivitiException("Unsupported by LDAP user manager");
  }

  @Override
  public void delete(UserEntity entity, boolean fireDeleteEvent) {
    throw new ActivitiException("Unsupported by LDAP user manager");
  }

}
