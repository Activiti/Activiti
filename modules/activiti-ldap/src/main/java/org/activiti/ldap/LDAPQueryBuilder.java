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

import java.text.MessageFormat;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Rdn;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class with overridable methods that are called when doing the calls to the ldap system.
 * You can extend this class and plug it into the {@link LDAPConfigurator} if the default
 * queries are inadequate for your use case.
 * 
 * @author Joram Barrez
 */
public class LDAPQueryBuilder {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(LDAPQueryBuilder.class);
  
  public String buildQueryByUserId(LDAPConfigurator ldapConfigurator, String userId) {
    String searchExpression = null;
    if (ldapConfigurator.getQueryUserByUserId() != null) {
      searchExpression = MessageFormat.format(ldapConfigurator.getQueryUserByUserId(), userId);
    } else {
      searchExpression = userId;
    }
    return searchExpression;
  }
  
  public String buildQueryGroupsForUser(final LDAPConfigurator ldapConfigurator, final String userId) {
    String searchExpression = null;
    if (ldapConfigurator.getQueryGroupsForUser() != null) {
      
      // Fetch the dn of the user 
      LDAPTemplate ldapTemplate = new LDAPTemplate(ldapConfigurator);
      String userDn = ldapTemplate.execute(new LDAPCallBack<String>() {
        
        public String executeInContext(InitialDirContext initialDirContext) {
          
          String userDnSearch = buildQueryByUserId(ldapConfigurator, userId);
          try {
        	String baseDn = ldapConfigurator.getUserBaseDn() != null ? ldapConfigurator.getUserBaseDn() : ldapConfigurator.getBaseDn();
            NamingEnumeration< ? > namingEnum = initialDirContext.search(baseDn, userDnSearch, createSearchControls(ldapConfigurator));
            while (namingEnum.hasMore()) { // Should be only one
              SearchResult result = (SearchResult) namingEnum.next();
              return result.getNameInNamespace();
            }
            namingEnum.close();
          } catch (NamingException e) {
            LOGGER.debug("Could not find user dn : " + e.getMessage(), e);
          }
          return null;
        }
        
      });
      
      searchExpression = MessageFormat.format(ldapConfigurator.getQueryGroupsForUser(), Rdn.escapeValue(userDn));
      
    } else {
      searchExpression = userId;
    }
    return searchExpression;
  }
  
  public String buildQueryByFullNameLike(final LDAPConfigurator ldapConfigurator, String searchText) {
    String searchExpression = null;
    if (ldapConfigurator.getQueryUserByFullNameLike() != null) {
      searchExpression = MessageFormat.format(ldapConfigurator.getQueryUserByFullNameLike(), 
              ldapConfigurator.getUserFirstNameAttribute(),
              searchText,
              ldapConfigurator.getUserLastNameAttribute(),
              searchText);
    } else {
      throw new ActivitiIllegalArgumentException("No 'queryUserByFullNameLike' configured");
    }
    return searchExpression;
  }
  
  protected SearchControls createSearchControls(LDAPConfigurator ldapConfigurator) {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setTimeLimit(ldapConfigurator.getSearchTimeLimit());
    return searchControls;
  }

}
