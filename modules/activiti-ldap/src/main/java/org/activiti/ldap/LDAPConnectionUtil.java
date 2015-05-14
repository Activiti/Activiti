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

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing LDAP connections.
 * 
 * @author jbarrez
 */
public class LDAPConnectionUtil {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(LDAPConnectionUtil.class); 
  
  public static InitialDirContext creatDirectoryContext(LDAPConfigurator ldapConfigurator) {
    return createDirectoryContext(ldapConfigurator, ldapConfigurator.getUser(), ldapConfigurator.getPassword());
  }
  
  public static InitialDirContext createDirectoryContext(LDAPConfigurator ldapConfigurator, String principal, String credentials) {
    Properties properties = new Properties();
    properties.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfigurator.getInitialContextFactory()); 
    properties.put(Context.PROVIDER_URL, ldapConfigurator.getServer() + ":" + ldapConfigurator.getPort());
    properties.put(Context.SECURITY_AUTHENTICATION, ldapConfigurator.getSecurityAuthentication());
    properties.put(Context.SECURITY_PRINCIPAL, principal);
    properties.put(Context.SECURITY_CREDENTIALS, credentials);
    
    if (ldapConfigurator.getCustomConnectionParameters() != null) {
      for (String customParameter : ldapConfigurator.getCustomConnectionParameters().keySet()) {
        properties.put(customParameter, ldapConfigurator.getCustomConnectionParameters().get(customParameter));
      }
    }

    InitialDirContext context;
    try {
      context = new InitialDirContext(properties);
    } catch (NamingException e) {
    	LOGGER.warn("Could not create InitialDirContext for LDAP connection : " + e.getMessage());
      throw new ActivitiException("Could not create InitialDirContext for LDAP connection : " + e.getMessage(), e);
    }
    return context; 
  }
  
  
  public static void closeDirectoryContext(InitialDirContext initialDirContext) {
    try {
      initialDirContext.close();
    } catch (NamingException e) {
      LOGGER.warn("Could not close InitialDirContext correctly!", e);
    }
  }
	
}
