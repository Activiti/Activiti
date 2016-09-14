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
package org.activiti.app.service.runtime;

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
