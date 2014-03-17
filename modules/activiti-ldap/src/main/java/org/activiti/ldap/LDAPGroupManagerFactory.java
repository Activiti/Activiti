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

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.runtime.ClockReader;
import org.activiti.ldap.LDAPGroupCache.LDAPGroupCacheListener;

/**
 * {@link SessionFactory} responsible for creating the {@link LDAPGroupManager}.
 * Is plugged into the {@link ProcessEngineConfiguration} automatically through the {@link LDAPConfigurator}.
 * 
 * @author Joram Barrez
 */
public class LDAPGroupManagerFactory implements SessionFactory {

  protected LDAPConfigurator ldapConfigurator;
  
  protected LDAPGroupCache ldapGroupCache;
  protected LDAPGroupCacheListener ldapCacheListener;
  
	public LDAPGroupManagerFactory(LDAPConfigurator ldapConfigurator, ClockReader clockReader) {
    this.ldapConfigurator = ldapConfigurator;
    
    if (ldapConfigurator.getGroupCacheSize() > 0) {
      ldapGroupCache = new LDAPGroupCache(ldapConfigurator.getGroupCacheSize(), ldapConfigurator.getGroupCacheExpirationTime(), clockReader);
      if (ldapCacheListener != null) {
        ldapGroupCache.setLdapCacheListener(ldapCacheListener);
      }
    }
  }
	
	@Override
  public Class<?> getSessionType() {
	  return GroupIdentityManager.class;
  }

	@Override
  public Session openSession() {
	  if (ldapGroupCache == null) {
	    return new LDAPGroupManager(ldapConfigurator);
	  } else {
	    return new LDAPGroupManager(ldapConfigurator, ldapGroupCache);
	  }
  }

  public LDAPConfigurator getLdapConfigurator() {
    return ldapConfigurator;
  }

  public void setLdapConfigurator(LDAPConfigurator ldapConfigurator) {
    this.ldapConfigurator = ldapConfigurator;
  }
  
  public LDAPGroupCache getLdapGroupCache() {
    return ldapGroupCache;
  }
  
  public void setLdapGroupCache(LDAPGroupCache ldapGroupCache) {
    this.ldapGroupCache = ldapGroupCache;
  }
  
  public LDAPGroupCacheListener getLdapCacheListener() {
    return ldapCacheListener;
  }
  
  public void setLdapCacheListener(LDAPGroupCacheListener ldapCacheListener) {
    this.ldapCacheListener = ldapCacheListener;
  }
  
}
