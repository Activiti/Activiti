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
package com.activiti.service.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AppVersionService {
	
	private static final Logger logger = LoggerFactory.getLogger(AppVersionService.class);
	
	private static final String VERSION_FILE = "/version.properties";
	private static final String TYPE = "type";
	private static final String MAJOR_VERSION = "version.major";
	private static final String MINOR_VERSION = "version.minor";
	private static final String REVISION_VERSION = "version.revision";
	private static final String EDITION = "version.edition";
	private static final String MAVEN_VERSION = "maven.version";
	private static final String GIT_VERSION = "git.version";
	
	private Map<String, String> versionInfo;
	
	public Map<String, String> getVersionInfo() {
		if (versionInfo == null) {
			Properties properties = new Properties();
			try {
	            properties.load(this.getClass().getResourceAsStream(VERSION_FILE));
            } catch (IOException e) {
            	logger.warn("Could not load version.properties", e);
            }
			
			Map<String, String> temp = new HashMap<String, String>();
			putIfExists(properties, TYPE, temp, "type");
			putIfExists(properties, MAJOR_VERSION, temp, "majorVersion");
			putIfExists(properties, MINOR_VERSION, temp, "minorVersion");
			putIfExists(properties, REVISION_VERSION, temp, "revisionVersion");
			putIfExists(properties, EDITION, temp, "edition");
			putIfExists(properties, MAVEN_VERSION, temp, "mavenVersion");
			putIfExists(properties, GIT_VERSION, temp, "gitVersion");
			versionInfo = temp;
		}
		return versionInfo;
	}
	
	protected void putIfExists(Properties properties, String property, Map<String, String> map, String mapKey) {
		String value = properties.getProperty(property);
		if (value != null) {
			map.put(mapKey, value);
		}
	}
 
}
