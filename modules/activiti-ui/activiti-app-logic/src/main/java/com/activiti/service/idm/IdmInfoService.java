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
package com.activiti.service.idm;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Simple wrapper for IDM related information. Currently very, very simple.
 * But who knows, maybe one day this little guy might get bigger!
 * 
 * @author Joram Barrez
 */
@Service
public class IdmInfoService {

	public static final String KEY_LDAP_ENABLED = "ldap.authentication.enabled";
	
	public static final String TYPE_DEFAULT = "default";
	public static final String TYPE_EXTERNAL_LDAP = "external-ldap";
	
	@Autowired
	private Environment environment;
	
	private String idmType;
	
	@PostConstruct
	protected void postConstruct() {
		Boolean isLdapEnabled = environment.getProperty(KEY_LDAP_ENABLED, Boolean.class, false);
		if (isLdapEnabled) {
			idmType = TYPE_EXTERNAL_LDAP;
		} else {
			idmType = TYPE_DEFAULT;
		}
	}

	public String getIdmType() {
		return idmType;
	}

	public void setIdmType(String idmType) {
		this.idmType = idmType;
	}
	
}
