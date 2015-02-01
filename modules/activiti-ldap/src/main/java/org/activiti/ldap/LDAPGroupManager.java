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
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;

/**
 * Implementation of the {@link GroupIdentityManager} interface specifically for LDAP.
 * 
 * Note that only a few methods are actually implemented, as many of the operations 
 * (save, update, etc.) are done on the LDAP system directly. 
 * 
 * @author Joram Barrez
 */
public class LDAPGroupManager extends AbstractManager implements GroupIdentityManager {

  protected LDAPConfigurator ldapConfigurator;
  protected LDAPGroupCache ldapGroupCache;
  
	public LDAPGroupManager(LDAPConfigurator ldapConfigurator) {
		this.ldapConfigurator = ldapConfigurator;
	}
	
	public LDAPGroupManager(LDAPConfigurator ldapConfigurator, LDAPGroupCache ldapGroupCache) {
	  this.ldapConfigurator = ldapConfigurator;
	  this.ldapGroupCache = ldapGroupCache;
	}

  @Override
  public Group createNewGroup(String groupId) {
    throw new ActivitiException("LDAP group manager doesn't support creating a new group");
  }

  @Override
  public void insertGroup(Group group) {
    throw new ActivitiException("LDAP group manager doesn't support inserting a group");
  }

  @Override
  public void updateGroup(Group updatedGroup) {
    throw new ActivitiException("LDAP group manager doesn't support updating a group");
  }
  
  @Override
  public boolean isNewGroup(Group group) {
  	throw new ActivitiException("LDAP group manager doesn't support inserting or updating a group");
  }

  @Override
  public void deleteGroup(String groupId) {
    throw new ActivitiException("LDAP group manager doesn't support deleting a group");
  }

  @Override
  public GroupQuery createNewGroupQuery() {
    return new GroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutor());
  }

  @Override
  public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
    // Only support for groupMember() at the moment
    if (query.getUserId() != null) {
      return findGroupsByUser(query.getUserId());
    } else {
      throw new ActivitiIllegalArgumentException("This query is not supported by the LDAPGroupManager");
    }
  }

  @Override
  public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
    return findGroupByQueryCriteria(query, null).size(); // Is there a generic way to do a count(*) in ldap?
  }

  @Override
  public List<Group> findGroupsByUser(final String userId) {
    
    // First try the cache (if one is defined)
    if (ldapGroupCache != null) {
      List<Group> groups = ldapGroupCache.get(userId);
      if (groups != null) {
        return groups;
      }
    }
    
    // Do the search against Ldap
    LDAPTemplate ldapTemplate = new LDAPTemplate(ldapConfigurator);
    return ldapTemplate.execute(new LDAPCallBack<List<Group>>() {
      
      public List<Group> executeInContext(InitialDirContext initialDirContext) {
        
        String searchExpression = ldapConfigurator.getLdapQueryBuilder().buildQueryGroupsForUser(ldapConfigurator, userId);
        
        List<Group> groups = new ArrayList<Group>();
        try {
          String baseDn = ldapConfigurator.getGroupBaseDn() != null ? ldapConfigurator.getGroupBaseDn() : ldapConfigurator.getBaseDn();
          NamingEnumeration< ? > namingEnum = initialDirContext.search(baseDn, searchExpression, createSearchControls());
          while (namingEnum.hasMore()) { // Should be only one
            SearchResult result = (SearchResult) namingEnum.next();
            
            GroupEntity group = new GroupEntity();
            if (ldapConfigurator.getGroupIdAttribute() != null) {
              group.setId(result.getAttributes().get(ldapConfigurator.getGroupIdAttribute()).get().toString());
            }
            if (ldapConfigurator.getGroupNameAttribute() != null) {
              group.setName(result.getAttributes().get(ldapConfigurator.getGroupNameAttribute()).get().toString());
            }
            if (ldapConfigurator.getGroupTypeAttribute() != null) {
              group.setType(result.getAttributes().get(ldapConfigurator.getGroupTypeAttribute()).get().toString());
            }
            groups.add(group);
          }
          
          namingEnum.close();
          
          // Cache results for later
          if (ldapGroupCache != null) {
            ldapGroupCache.add(userId, groups);
          }
          
          return groups;
          
        } catch (NamingException e) {
          throw new ActivitiException("Could not find groups for user " + userId, e);
        }
      }
      
    });
  }

  @Override
  public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    throw new ActivitiException("LDAP group manager doesn't support querying");
  }

  @Override
  public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
    throw new ActivitiException("LDAP group manager doesn't support querying");
  }
  
  protected SearchControls createSearchControls() {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setTimeLimit(ldapConfigurator.getSearchTimeLimit());
    return searchControls;
  }
	
}
