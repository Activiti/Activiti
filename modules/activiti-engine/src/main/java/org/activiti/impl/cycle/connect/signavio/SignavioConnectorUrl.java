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
 * 
 * @author christian.lipphardt@camunda.com
 */
public class SignavioConnectorUrl {

	// TODO: read SIGNAVIO_URL + PORT from configuration
	public static final String SIGNAVIO_URL = "http://127.0.0.1";
	public static final String PORT = "8080";
	public static final String HOST = SIGNAVIO_URL + ":" + PORT;
	
	public static final String SERVER_URL = HOST + "/p";
	
	public static final String REGISTRATION_URL = SERVER_URL + "/register";
	public static final String LOGIN_URL = SERVER_URL + "/login";
	public static final String EDITOR_URL = SERVER_URL + "/editor";
	public static final String EXPLORER_URL = SERVER_URL + "/explorer";
	public static final String MODEL_URL = SERVER_URL + "/model";
	public static final String DIRECTORY_URL = SERVER_URL + "/directory";
	
	public static final String MASHUP_URL = HOST + "/mashup";
	
}
