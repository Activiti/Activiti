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
package org.activiti.impl.cycle.connect.signavio;

/**
 * Object used to configure signavio connector. Candidate for 
 * Entity to save config later on.
 * 
 * ALL url's have a trailing "/".
 * 
 * @author bernd.ruecker@camunda.com
 */
public class SignavioConnectorConfiguration {

	/**
	 * default URL
	 */
	private String signavioUrl = "http://127.0.0.1:8080/p/";
	
	public static String REGISTRATION_URL_SUFFIX = "register/";
	public static String LOGIN_URL_SUFFIX = "login/";
	public static String EDITOR_URL_SUFFIX = "editor/";
	public static String EXPLORER_URL_SUFFIX = "explorer/";
	public static String MODEL_URL_SUFFIX = "model/";
	public static String DIRECTORY_URL_SUFFIX = "directory/";	
	public static String MASHUP_URL_SUFFIX = "mashup/";
	
	public SignavioConnectorConfiguration() {		
	}
	
	public SignavioConnectorConfiguration(String signavioUrl) {
		setSignavioUrl(signavioUrl);
	}

	public String getSignavioUrl() {
		return signavioUrl;
	}

	public void setSignavioUrl(String signavioUrl) {
		if (signavioUrl!=null && !signavioUrl.endsWith("/")) {
			signavioUrl = signavioUrl + "/";
		}
		this.signavioUrl = signavioUrl;
	}
	
	public String getRegistrationUrl() {
		return getSignavioUrl() + REGISTRATION_URL_SUFFIX;
	}

	public String getLoginUrl() {
		return getSignavioUrl() + LOGIN_URL_SUFFIX;
	}

	public String getEditorUrl() {
		return getSignavioUrl() + EDITOR_URL_SUFFIX;
	}

	public String getExplorerUrl() {
		return getSignavioUrl() + EXPLORER_URL_SUFFIX;
	}

	public String getModelUrl() {
		return getSignavioUrl() + MODEL_URL_SUFFIX;
	}
	
	public String getDirectoryUrl() {
		return getSignavioUrl() + DIRECTORY_URL_SUFFIX;
	}

	public String getMashupUrl() {
		return getSignavioUrl() + MASHUP_URL_SUFFIX;
	}
}
