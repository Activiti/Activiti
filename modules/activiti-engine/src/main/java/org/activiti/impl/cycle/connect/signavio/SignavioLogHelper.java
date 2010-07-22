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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.CookieSetting;
import org.restlet.data.Response;
import org.restlet.util.Series;

/**
 * 
 * @author christian.lipphardt@camunda.com
 */
public class SignavioLogHelper {

	public static void logCookieAndBody(Logger log, Response response) {
		// Retrieve the cookie from the server and output it
		Series<CookieSetting> cookie = response.getCookieSettings();
		if (cookie != null && !cookie.isEmpty()) {
			Map<String, String> cookieVals = cookie.getValuesMap();
			for (Iterator<Entry<String, String>> it = cookieVals.entrySet().iterator(); it.hasNext();) {
				Entry<String, String> entry = it.next();
			  log.finest("CookieParam - Key: " + entry.getKey() + " = " + entry.getValue());
			}
		}
	
		log.finest("Response - Header: " + response.getStatus());
		if (response.getStatus().isRedirection()) {
			log.finest("Response - LocationRef (Redirection): " + response.getLocationRef().toString());
		}
	}

	public static void logJSONArray(Logger log, JSONArray jsonArray) {
		try {
			log.info(jsonArray.toString(2));
		} catch (JSONException je) {
		  log.log(Level.SEVERE, "JSONException while trying to log JSONArray", je);
		}
	}
	
	

}
