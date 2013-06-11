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

import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Internal class used to simplify ldap calls by wrapping the
 * actual ldap logic in a {@link LDAPCallBack}.
 * 
 * @author Joram Barrez
 */
public class LDAPTemplate {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(LDAPTemplate.class);
  
  protected LDAPConfigurator ldapConfigurator;
  
  public LDAPTemplate(LDAPConfigurator ldapConfigurator) {
    this.ldapConfigurator = ldapConfigurator;
  }
  
  public <T> T execute(LDAPCallBack<T> ldapCallBack) {
    InitialDirContext initialDirContext = null;
    try {
      initialDirContext = LDAPConnectionUtil.creatDirectoryContext(ldapConfigurator);
    } catch (Exception e) {
      LOGGER.info("Could not create LDAP connection : " + e.getMessage(), e);
    }
    T result = ldapCallBack.executeInContext(initialDirContext);
    LDAPConnectionUtil.closeDirectoryContext(initialDirContext);
    return result;
  }

  
  public LDAPConfigurator getLdapConfigurator() {
    return ldapConfigurator;
  }

  public void setLdapConfigurator(LDAPConfigurator ldapConfigurator) {
    this.ldapConfigurator = ldapConfigurator;
  }

}
