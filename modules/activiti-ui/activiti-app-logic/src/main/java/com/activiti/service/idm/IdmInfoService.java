/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
