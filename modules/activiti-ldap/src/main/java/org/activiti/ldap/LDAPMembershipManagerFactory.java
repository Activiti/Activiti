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

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.MembershipEntity;
import org.activiti.engine.impl.persistence.entity.MembershipIdentityManager;

/**
 * {@link SessionFactory} responsible for creating a {@link Session}
 * that manages {@link MembershipEntity}s.
 * 
 * For LDAP, this will not do anything and even throw an exception
 * when trying to use, as memberships are managed by the ldap system itself.
 * 
 * @author Joram Barrez
 */
public class LDAPMembershipManagerFactory implements SessionFactory {

	protected LDAPConfigurator ldapConfigurator;
	
	public LDAPMembershipManagerFactory(LDAPConfigurator ldapConfigurator) {
    this.ldapConfigurator = ldapConfigurator;
  }
	
	@Override
  public Class<?> getSessionType() {
	  return MembershipIdentityManager.class;
  }

	@Override
  public Session openSession() {
	  throw new UnsupportedOperationException("Memberships are not supported in ldap");
  }

  public LDAPConfigurator getLdapConfigurator() {
    return ldapConfigurator;
  }
  
  public void setLdapConfigurator(LDAPConfigurator ldapConfigurator) {
    this.ldapConfigurator = ldapConfigurator;
  }

}
