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

import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class LDAPUserManager extends AbstractManager implements UserIdentityManager {

  private static Logger logger = LoggerFactory.getLogger(LDAPUserManager.class);

  protected LDAPConfigurator ldapConfigurator;

  public LDAPUserManager(LDAPConfigurator ldapConfigurator) {
    this.ldapConfigurator = ldapConfigurator;
  }
  
  @Override
  public User createNewUser(String userId) {
    throw new ActivitiException("LDAP user manager doesn't support creating a new user");
  }


  @Override
  public void insertUser(User user) {
    throw new ActivitiException("LDAP user manager doesn't support inserting a new user");
  }


  @Override
  public void updateUser(UserEntity updatedUser) {
    throw new ActivitiException("LDAP user manager doesn't support updating a user");
  }


  @Override
  public UserEntity findUserById(final String userId) {
    LDAPTemplate ldapTemplate = new LDAPTemplate(ldapConfigurator);
    return ldapTemplate.execute(new LDAPCallBack<UserEntity>() {

      public UserEntity executeInContext(InitialDirContext initialDirContext) {
        try {
        
          String searchExpression = ldapConfigurator.getLdapQueryBuilder().buildQueryByUserId(ldapConfigurator, userId);

          NamingEnumeration< ? > namingEnum = initialDirContext.search(ldapConfigurator.getBaseDn(), searchExpression, createSearchControls());
          UserEntity user = new UserEntity();
          while (namingEnum.hasMore()) { // Should be only one
            SearchResult result = (SearchResult) namingEnum.next();
            
            user.setId(userId);
            
            if (ldapConfigurator.getUserFirstNameAttribute() != null) {
              user.setFirstName(result.getAttributes().get(ldapConfigurator.getUserFirstNameAttribute()).get().toString());
            }
            if (ldapConfigurator.getUserLastNameAttribute() != null) {
              user.setLastName(result.getAttributes().get(ldapConfigurator.getUserLastNameAttribute()).get().toString());
            }
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
  public void deleteUser(String userId) {
    throw new ActivitiException("LDAP user manager doesn't support deleting a user");
  }


  @Override
  public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }


  @Override
  public long findUserCountByQueryCriteria(UserQueryImpl query) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }


  @Override
  public List<Group> findGroupsByUser(String userId) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }


  @Override
  public UserQuery createNewUserQuery() {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }


  @Override
  public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }


  @Override
  public List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
  }


  @Override
  public List<User> findPotentialStarterUsers(String proceDefId) {
    throw new ActivitiException("LDAP user manager doesn't support querying");
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
  public Boolean checkPassword(final String userId, final String password) {
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
            NamingEnumeration< ? > namingEnum = initialDirContext.search(ldapConfigurator.getBaseDn(), 
                    searchExpression, createSearchControls());
            
            while (namingEnum.hasMore()) { // Should be only one
              SearchResult result = (SearchResult) namingEnum.next();
              userDn = result.getNameInNamespace();
            }
            namingEnum.close();

          } catch (NamingException ne) {
            logger.info("Could not authenticate user " + userId + " : " + ne.getMessage(), ne);
            return false;
          }

          // Now we have the user DN, we can need to create a connection it
          // ('bind' in ldap lingo)
          // to check if the user is valid
          if (userDn != null) {
            InitialDirContext verificationContext = null;
            try {
              verificationContext = LDAPConnectionUtil.createDirectoryContext(ldapConfigurator, userDn, password);
            } catch (ActivitiException e) {
              // Do nothing, an exception will be thrown if the login fails
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

}
