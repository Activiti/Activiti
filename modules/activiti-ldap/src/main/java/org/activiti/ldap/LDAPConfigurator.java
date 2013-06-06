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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurator;


/**
 * @author Joram Barrez
 */
public class LDAPConfigurator implements ProcessEngineConfigurator {
  
  // Server connection params
  protected String server;
  protected int port;
  protected String user;
  protected String password;
  protected String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
  protected String securityAuthentication = "simple";
  
  // For parameters like connection pooling settings, etc.
  protected Map<String, String> customConnectionParameters = new HashMap<String, String>();
  
  // Query configuration
  protected String baseDn;
  protected int searchTimeLimit = 0; // Default '0' == wait forever

  protected String queryUserByUserId;
  protected String queryGroupsForUser;
  
  // Attribute names
  protected String userIdAttribute;
  protected String userFirstNameAttribute;
  protected String userLastNameAttribute;
  
  protected String groupIdAttribute;
  protected String groupNameAttribute;
  protected String groupTypeAttribute;
  
  // Pluggable factories
  protected LDAPUserManagerFactory ldapUserManagerFactory;
  protected LDAPGroupManagerFactory ldapGroupManagerFactory;
  protected LDAPMembershipManagerFactory ldapMembershipManagerFactory;
  
  // Pluggable query helper bean
  protected LDAPQueryBuilder ldapQueryBuilder = new LDAPQueryBuilder();
  
  // Group caching
  protected int groupCacheSize = -1;
  protected long groupCacheExpirationTime = 3600000L; // default: one hour
  
  public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
    LDAPUserManagerFactory ldapUserManagerFactory = getLdapUserManagerFactory();
    processEngineConfiguration.getSessionFactories().put(ldapUserManagerFactory.getSessionType(), ldapUserManagerFactory);
    
    LDAPGroupManagerFactory ldapGroupManagerFactory = getLdapGroupManagerFactory();
    processEngineConfiguration.getSessionFactories().put(ldapGroupManagerFactory.getSessionType(), ldapGroupManagerFactory);
    
  }
  
  // Can be overwritten for custom factories
  
  protected LDAPUserManagerFactory getLdapUserManagerFactory() {
    if (this.ldapUserManagerFactory != null) {
      this.ldapUserManagerFactory.setLdapConfigurator(this);
      return this.ldapUserManagerFactory;
    }
    return new LDAPUserManagerFactory(this);
  }
  
  protected LDAPGroupManagerFactory getLdapGroupManagerFactory() {
    if (this.ldapGroupManagerFactory != null) {
      this.ldapGroupManagerFactory.setLdapConfigurator(this);
      return this.ldapGroupManagerFactory;
    }
    return new LDAPGroupManagerFactory(this);
  }
  
  protected LDAPMembershipManagerFactory getLdapMembershipManagerFactory() {
    if (this.ldapMembershipManagerFactory != null) {
      this.ldapMembershipManagerFactory.setLdapConfigurator(this);
    }
    return new LDAPMembershipManagerFactory(this);
  }
  
  
  // Getters and Setters
  
  public String getServer() {
    return server;
  }
  
  public void setServer(String server) {
    this.server = server;
  }
  
  public int getPort() {
    return port;
  }
  
  public void setPort(int port) {
    this.port = port;
  }
  
  public String getUser() {
    return user;
  }
  
  public void setUser(String user) {
    this.user = user;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public String getInitialContextFactory() {
    return initialContextFactory;
  }
  
  public void setInitialContextFactory(String initialContextFactory) {
    this.initialContextFactory = initialContextFactory;
  }

  public String getSecurityAuthentication() {
    return securityAuthentication;
  }
  
  public void setSecurityAuthentication(String securityAuthentication) {
    this.securityAuthentication = securityAuthentication;
  }
  
  public Map<String, String> getCustomConnectionParameters() {
    return customConnectionParameters;
  }
  
  public void setCustomConnectionParameters(Map<String, String> customConnectionParameters) {
    this.customConnectionParameters = customConnectionParameters;
  }
  
  public String getBaseDn() {
    return baseDn;
  }
  
  public void setBaseDn(String baseDn) {
    this.baseDn = baseDn;
  }
  
  public int getSearchTimeLimit() {
    return searchTimeLimit;
  }
  
  public void setSearchTimeLimit(int searchTimeLimit) {
    this.searchTimeLimit = searchTimeLimit;
  }
  
  public String getQueryUserByUserId() {
    return queryUserByUserId;
  }

  public void setQueryUserByUserId(String queryUserByUserId) {
    this.queryUserByUserId = queryUserByUserId;
  }

  public String getQueryGroupsForUser() {
    return queryGroupsForUser;
  }
  
  public void setQueryGroupsForUser(String queryGroupsForUser) {
    this.queryGroupsForUser = queryGroupsForUser;
  }

  public String getUserIdAttribute() {
    return userIdAttribute;
  }
  
  public void setUserIdAttribute(String userIdAttribute) {
    this.userIdAttribute = userIdAttribute;
  }
  
  public String getUserFirstNameAttribute() {
    return userFirstNameAttribute;
  }

  public void setUserFirstNameAttribute(String userFirstNameAttribute) {
    this.userFirstNameAttribute = userFirstNameAttribute;
  }
  
  public String getUserLastNameAttribute() {
    return userLastNameAttribute;
  }
  
  public void setUserLastNameAttribute(String userLastNameAttribute) {
    this.userLastNameAttribute = userLastNameAttribute;
  }

  public String getGroupIdAttribute() {
    return groupIdAttribute;
  }
  
  public void setGroupIdAttribute(String groupIdAttribute) {
    this.groupIdAttribute = groupIdAttribute;
  }

  public String getGroupNameAttribute() {
    return groupNameAttribute;
  }
  
  public void setGroupNameAttribute(String groupNameAttribute) {
    this.groupNameAttribute = groupNameAttribute;
  }
  
  public String getGroupTypeAttribute() {
    return groupTypeAttribute;
  }
  
  public void setGroupTypeAttribute(String groupTypeAttribute) {
    this.groupTypeAttribute = groupTypeAttribute;
  }

  public void setLdapUserManagerFactory(LDAPUserManagerFactory ldapUserManagerFactory) {
    this.ldapUserManagerFactory = ldapUserManagerFactory;
  }
  
  public void setLdapGroupManagerFactory(LDAPGroupManagerFactory ldapGroupManagerFactory) {
    this.ldapGroupManagerFactory = ldapGroupManagerFactory;
  }
  
  public void setLdapMembershipManagerFactory(LDAPMembershipManagerFactory ldapMembershipManagerFactory) {
    this.ldapMembershipManagerFactory = ldapMembershipManagerFactory;
  }

  public LDAPQueryBuilder getLdapQueryBuilder() {
    return ldapQueryBuilder;
  }

  public void setLdapQueryBuilder(LDAPQueryBuilder ldapQueryBuilder) {
    this.ldapQueryBuilder = ldapQueryBuilder;
  }
  
  public int getGroupCacheSize() {
    return groupCacheSize;
  }
  
  public void setGroupCacheSize(int groupCacheSize) {
    this.groupCacheSize = groupCacheSize;
  }
  
  public long getGroupCacheExpirationTime() {
    return groupCacheExpirationTime;
  }
  
  public void setGroupCacheExpirationTime(long groupCacheExpirationTime) {
    this.groupCacheExpirationTime = groupCacheExpirationTime;
  }

}
