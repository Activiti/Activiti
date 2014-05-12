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
import java.util.HashMap;
import java.util.Map;

import javax.naming.directory.InitialDirContext;
import javax.naming.spi.InitialContextFactory;

import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.ClockReader;


/**
 * A {@link ProcessEngineConfigurator} that integrates a LDAP system with the Activiti process engine.
 * The LDAP system will be consulted primarily for getting user information and in particular
 * for fetching groups of a user.
 * 
 * This class is extensible and many methods can be overriden when the default behavior
 * is not fitting your use case.
 * 
 * Check the docs (speficifally the setters) to see how this class can be tweaked.
 * 
 * @author Joram Barrez
 */
public class LDAPConfigurator extends AbstractProcessEngineConfigurator {
  
  /* Server connection params */
  
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
  protected String userBaseDn;
  protected String groupBaseDn;
  protected int searchTimeLimit = 0; // Default '0' == wait forever

  protected String queryUserByUserId;
  protected String queryGroupsForUser;
  protected String queryUserByFullNameLike;
  
  // Attribute names
  protected String userIdAttribute;
  protected String userFirstNameAttribute;
  protected String userLastNameAttribute;
  protected String userEmailAttribute;
  
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

  // Cache clock
  private Clock clock;

  public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
  	// Nothing to do
  }
  
  public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
    clock = processEngineConfiguration.getClock();
    LDAPUserManagerFactory ldapUserManagerFactory = getLdapUserManagerFactory();
    processEngineConfiguration.getSessionFactories().put(ldapUserManagerFactory.getSessionType(), ldapUserManagerFactory);
    
    LDAPGroupManagerFactory ldapGroupManagerFactory = getLdapGroupManagerFactory(clock);
    processEngineConfiguration.getSessionFactories().put(ldapGroupManagerFactory.getSessionType(), ldapGroupManagerFactory);
    
  }
  
  // Can be overwritten for custom factories //////////////////////////////////////////////////
  
  protected LDAPUserManagerFactory getLdapUserManagerFactory() {
    if (this.ldapUserManagerFactory != null) {
      this.ldapUserManagerFactory.setLdapConfigurator(this);
      return this.ldapUserManagerFactory;
    }
    return new LDAPUserManagerFactory(this);
  }
  
  protected LDAPGroupManagerFactory getLdapGroupManagerFactory(ClockReader clockReader) {
    if (this.ldapGroupManagerFactory != null) {
      this.ldapGroupManagerFactory.setLdapConfigurator(this);
      return this.ldapGroupManagerFactory;
    }
    return new LDAPGroupManagerFactory(this, clockReader);
  }
  
  protected LDAPMembershipManagerFactory getLdapMembershipManagerFactory() {
    if (this.ldapMembershipManagerFactory != null) {
      this.ldapMembershipManagerFactory.setLdapConfigurator(this);
    }
    return new LDAPMembershipManagerFactory(this);
  }
  
  
  // Getters and Setters //////////////////////////////////////////////////
  
  public String getServer() {
    return server;
  }
  
  /**
   * The server on which the LDAP system can be reached.
   * For example 'ldap://localhost:33389'.
   */
  public void setServer(String server) {
    this.server = server;
  }
  
  public int getPort() {
    return port;
  }
  
  /**
   * The port on which the LDAP system is running.
   */
  public void setPort(int port) {
    this.port = port;
  }
  
  public String getUser() {
    return user;
  }
  
  /**
   * The user id that is used to connect to the LDAP system.
   */
  public void setUser(String user) {
    this.user = user;
  }
  
  public String getPassword() {
    return password;
  }
  
  /**
   * The password that is used to connect to the LDAP system.
   */
  public void setPassword(String password) {
    this.password = password;
  }
  
  public String getInitialContextFactory() {
    return initialContextFactory;
  }
  
  /**
   * The {@link InitialContextFactory} name used to connect to the LDAP system.
   * 
   * By default set to 'com.sun.jndi.ldap.LdapCtxFactory'.
   */
  public void setInitialContextFactory(String initialContextFactory) {
    this.initialContextFactory = initialContextFactory;
  }

  public String getSecurityAuthentication() {
    return securityAuthentication;
  }
  
  /**
   * The value that is used for the 'java.naming.security.authentication' property
   * used to connect to the LDAP system.
   * 
   * By default set to 'simple'.
   */
  public void setSecurityAuthentication(String securityAuthentication) {
    this.securityAuthentication = securityAuthentication;
  }
  
  public Map<String, String> getCustomConnectionParameters() {
    return customConnectionParameters;
  }
  
  /**
   * Allows to set all LDAP connection parameters which do not have a dedicated setter.
   * See for example http://docs.oracle.com/javase/tutorial/jndi/ldap/jndi.html for custom 
   * properties. Such properties are for example to configure connection pooling, specific
   * security settings, etc.
   * 
   * All the provided parameters will be provided when creating a {@link InitialDirContext},
   * ie when a connection to the LDAP system is established.
   */
  public void setCustomConnectionParameters(Map<String, String> customConnectionParameters) {
    this.customConnectionParameters = customConnectionParameters;
  }
  
  public String getBaseDn() {
    return baseDn;
  }
  
  /**
   * The base 'distinguished name' (DN) from which the searches for users and groups are started.
   * 
   * Use {@link #setUserBaseDn(String)} or {@link #setGroupBaseDn(String)} when needing to
   * differentiate between user and group base DN.
   */
  public void setBaseDn(String baseDn) {
    this.baseDn = baseDn;
  }
  
  public String getUserBaseDn() {
	return userBaseDn;
  }

  /**
   * The base 'distinguished name' (DN) from which the searches for users are started.
   */
  public void setUserBaseDn(String userBaseDn) {
    this.userBaseDn = userBaseDn;
  }
	
  public String getGroupBaseDn() {
	return groupBaseDn;
  }
	
  /**
   * The base 'distinguished name' (DN) from which the searches for groups are started.
   */
  public void setGroupBaseDn(String groupBaseDn) {
	this.groupBaseDn = groupBaseDn;
  }
	
  public int getSearchTimeLimit() {
    return searchTimeLimit;
  }
  
  /**
   * The timeout that is used when doing a search in LDAP.
   * By default set to '0', which means 'wait forever'.
   */
  public void setSearchTimeLimit(int searchTimeLimit) {
    this.searchTimeLimit = searchTimeLimit;
  }
  
  public String getQueryUserByUserId() {
    return queryUserByUserId;
  }

  /**
   * The query that is executed when searching for a user by userId.
   * 
   * For example: (&amp;(objectClass=inetOrgPerson)(uid={0}))
   * 
   * Here, all the objects in LDAP with the class 'inetOrgPerson'
   * and who have the matching 'uid' attribute value will be returned.
   * 
   * As shown in the example, the user id is injected by the typical 
   * {@link MessageFormat}, ie by using <i>{0}</i>
   * 
   * If setting the query alone is insufficient for your specific
   * LDAP setup, you can alternatively plug in a different
   * {@link LDAPQueryBuilder}, which allows for more customization than
   * only the  query.
   */
  public void setQueryUserByUserId(String queryUserByUserId) {
    this.queryUserByUserId = queryUserByUserId;
  }

  public String getQueryGroupsForUser() {
    return queryGroupsForUser;
  }
  
  
  public String getQueryUserByFullNameLike() {
    return queryUserByFullNameLike;
  }
  
  /**
   * The query that is executed when searching for a user by full name.
   * 
   * For example: (&amp;(objectClass=inetOrgPerson)(|({0}=*{1}*)({2}={3})))
   * 
   * Here, all the objects in LDAP with the class 'inetOrgPerson'
   * and who have the matching first name or last name will be returned
   * 
   * Several things will be injected in the expression:
   * {0} : the first name attribute
   * {1} : the search text
   * {2} : the last name attribute
   * {3} : the search text
   * 
   * If setting the query alone is insufficient for your specific
   * LDAP setup, you can alternatively plug in a different
   * {@link LDAPQueryBuilder}, which allows for more customization than
   * only the  query.
   */
  public void setQueryUserByFullNameLike(String queryUserByFullNameLike) {
    this.queryUserByFullNameLike = queryUserByFullNameLike;
  }

  /**
   * The query that is executed when searching for the groups of a specific user.
   * 
   * For example: (&amp;(objectClass=groupOfUniqueNames)(uniqueMember={0}))
   * 
   * Here, all the objects in LDAP with the class 'groupOfUniqueNames'
   * and where the provided DN is a 'uniqueMember' are returned.
   * 
   * As shown in the example, the user id is injected by the typical 
   * {@link MessageFormat}, ie by using <i>{0}</i>
   * 
   * If setting the query alone is insufficient for your specific
   * LDAP setup, you can alternatively plug in a different
   * {@link LDAPQueryBuilder}, which allows for more customization than
   * only the  query.
   */
  public void setQueryGroupsForUser(String queryGroupsForUser) {
    this.queryGroupsForUser = queryGroupsForUser;
  }

  public String getUserIdAttribute() {
    return userIdAttribute;
  }
  
  /**
   * Name of the attribute that matches the user id.
   * 
   * This property is used when looking for a {@link User} object
   * and the mapping between the LDAP object and the Activiti {@link User} object
   * is done.
   * 
   * This property is optional and is only needed if searching for {@link User}
   * objects using the Activiti API.
   */
  public void setUserIdAttribute(String userIdAttribute) {
    this.userIdAttribute = userIdAttribute;
  }
  
  public String getUserFirstNameAttribute() {
    return userFirstNameAttribute;
  }

  /**
   * Name of the attribute that matches the user first name.
   * 
   * This property is used when looking for a {@link User} object
   * and the mapping between the LDAP object and the Activiti {@link User} object
   * is done.
   */
  public void setUserFirstNameAttribute(String userFirstNameAttribute) {
    this.userFirstNameAttribute = userFirstNameAttribute;
  }
  
  public String getUserLastNameAttribute() {
    return userLastNameAttribute;
  }
  
  /**
   * Name of the attribute that matches the user last name.
   * 
   * This property is used when looking for a {@link User} object
   * and the mapping between the LDAP object and the Activiti {@link User} object
   * is done.
   */
  public void setUserLastNameAttribute(String userLastNameAttribute) {
    this.userLastNameAttribute = userLastNameAttribute;
  }
  
  
  public String getUserEmailAttribute() {
    return userEmailAttribute;
  }

  /**
   * Name of the attribute that matches the user email.
   * 
   * This property is used when looking for a {@link User} object
   * and the mapping between the LDAP object and the Activiti {@link User} object
   * is done.
   */
  public void setUserEmailAttribute(String userEmailAttribute) {
    this.userEmailAttribute = userEmailAttribute;
  }

  public String getGroupIdAttribute() {
    return groupIdAttribute;
  }
  
  /**
   * Name of the attribute that matches the group id.
   * 
   * This property is used when looking for a {@link Group} object
   * and the mapping between the LDAP object and the Activiti {@link Group} object
   * is done.
   */
  public void setGroupIdAttribute(String groupIdAttribute) {
    this.groupIdAttribute = groupIdAttribute;
  }

  public String getGroupNameAttribute() {
    return groupNameAttribute;
  }
  
  /**
   * Name of the attribute that matches the group name.
   * 
   * This property is used when looking for a {@link Group} object
   * and the mapping between the LDAP object and the Activiti {@link Group} object
   * is done.
   */
  public void setGroupNameAttribute(String groupNameAttribute) {
    this.groupNameAttribute = groupNameAttribute;
  }
  
  public String getGroupTypeAttribute() {
    return groupTypeAttribute;
  }
  
  /**
   * Name of the attribute that matches the group type.
   * 
   * This property is used when looking for a {@link Group} object
   * and the mapping between the LDAP object and the Activiti {@link Group} object
   * is done.
   */
  public void setGroupTypeAttribute(String groupTypeAttribute) {
    this.groupTypeAttribute = groupTypeAttribute;
  }

  /**
   * Set a custom implementation of the {@link LDAPUserManagerFactory}
   * if the default implementation is not suitable.
   */
  public void setLdapUserManagerFactory(LDAPUserManagerFactory ldapUserManagerFactory) {
    this.ldapUserManagerFactory = ldapUserManagerFactory;
  }
  
  /**
   * Set a custom implementation of the {@link LDAPGroupManagerFactory}
   * if the default implementation is not suitable.
   */
  public void setLdapGroupManagerFactory(LDAPGroupManagerFactory ldapGroupManagerFactory) {
    this.ldapGroupManagerFactory = ldapGroupManagerFactory;
  }
  
  /**
   * Set a custom implementation of the {@link LDAPMembershipManagerFactory}
   * if the default implementation is not suitable.
   */
  public void setLdapMembershipManagerFactory(LDAPMembershipManagerFactory ldapMembershipManagerFactory) {
    this.ldapMembershipManagerFactory = ldapMembershipManagerFactory;
  }

  /**
   * Set a custom {@link LDAPQueryBuilder} if the default implementation is not suitable.
   * The {@link LDAPQueryBuilder} instance is used when the {@link LDAPUserManager} or
   * {@link LDAPGroupManager} does an actual query against the LDAP system.
   * 
   * The default implementation uses the properties as set on this instance
   * such as {@link #setQueryGroupsForUser(String)} and {@link #setQueryUserByUserId(String)}.
   */
  public LDAPQueryBuilder getLdapQueryBuilder() {
    return ldapQueryBuilder;
  }

  public void setLdapQueryBuilder(LDAPQueryBuilder ldapQueryBuilder) {
    this.ldapQueryBuilder = ldapQueryBuilder;
  }
  
  public int getGroupCacheSize() {
    return groupCacheSize;
  }
  
  /**
   * Allows to set the size of the {@link LDAPGroupCache}.
   * This is an LRU cache that caches groups for users and thus
   * avoids hitting the LDAP system each time the groups of 
   * a user needs to be known.
   * 
   * The cache will not be instantiated if the value is less then zero.
   * By default set to -1, so no caching is done.
   * 
   * Note that the group cache is instantiated on the {@link LDAPGroupManagerFactory}.
   * As such, if you have a custom implementation of the {@link LDAPGroupManagerFactory},
   * do not forget to add the group cache functionality.
   */
  public void setGroupCacheSize(int groupCacheSize) {
    this.groupCacheSize = groupCacheSize;
  }
  
  public long getGroupCacheExpirationTime() {
    return groupCacheExpirationTime;
  }
  
  /**
   * Sets the expiration time of the {@link LDAPGroupCache} in milliseconds.
   * When groups for a specific user are fetched, and if the group cache exists (see {@link #setGroupCacheSize(int)}),
   * the groups will be stored in this cache for the time set in this property.
   * ie. when the groups were fetched at 00:00 and the expiration time is 30 mins,
   * any fetch of the groups for that user after 00:30 will not come from the cache, but do 
   * a fetch again from the LDAP system. Likewise, everything group fetch for that user done
   * between 00:00 - 00:30 will come from the cache.
   * 
   * By default set to one hour.
   */
  public void setGroupCacheExpirationTime(long groupCacheExpirationTime) {
    this.groupCacheExpirationTime = groupCacheExpirationTime;
  }

}
