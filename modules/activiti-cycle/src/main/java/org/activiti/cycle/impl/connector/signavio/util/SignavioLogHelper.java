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
package org.activiti.cycle.impl.connector.signavio.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.util.Series;

/**
 * 
 * @author christian.lipphardt@camunda.com
 */
public class SignavioLogHelper {

  public static void logCookies(Logger log, Level logLevel, Series<CookieSetting> cookies) {
    if (cookies != null && !cookies.isEmpty()) {
      Map<String, String> cookieVals = cookies.getValuesMap();
      for (Iterator<Entry<String, String>> it = cookieVals.entrySet().iterator(); it.hasNext();) {
        Entry<String, String> entry = it.next();
        log.log(logLevel, "Cookie - (" + entry.getKey() + " = " + entry.getValue() + ")");
      }
    }
  }

  public static void logFormAttributes(Logger log, Level logLevel, Map attributes) {
    org.restlet.data.Form formAttributes = (Form) attributes.get("org.restlet.http.headers");
    if (formAttributes != null) {
      try {
        log.log(logLevel, "FormAttributes (URLDecoded) - " + URLDecoder.decode(formAttributes.getMatrixString(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // do nothing because utf-8 is everywhere... ;)
      }
    } else {
      log.log(logLevel, "FormAttributes (URLDecoded) - NONE");
    }
  }

  public static void logJSONArray(Logger log, Level logLevel, JSONArray jsonArray) {
    try {
      log.log(logLevel, jsonArray.toString(2));
    } catch (JSONException je) {
      log.log(Level.SEVERE, "JSONException while trying to log JSONArray", je);
    }
  }

  public static void logHttpRequest(Logger log, Level logLevel, Request request) {
    log.log(logLevel, "----- Start HttpRequest -----");
    log.log(logLevel, "Method - (" + request.getMethod() + ")");
    log.log(logLevel, "ResourceRef - (" + request.getResourceRef() + ")");
    if (request.getEntity() != null && request.getEntity().isAvailable()) {
      try {
        log.log(logLevel, "Body - (" + request.getEntity().getText() + ")");
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    if (request.getAttributes() != null) {
      logFormAttributes(log, logLevel, request.getAttributes());
    }
    log.log(logLevel, "----- End HttpRequest -----");
  }

  public static void logHttpResponse(Logger log, Level logLevel, Response response) {
    log.log(logLevel, "----- Start HttpResponse -----");
    if (response != null) {
      if (response.getStatus() != null) {
        log.log(logLevel, "HttpStatus - (" + response.getStatus() + ")");
      }
      if (response.getAttributes() != null) {
        logFormAttributes(log, logLevel, response.getAttributes());
      }
      if (response.getCookieSettings() != null) {
        logCookies(log, logLevel, response.getCookieSettings());
      }
      if (response.getEntity() != null && response.getEntity().isAvailable()) {
        // TODO: how to log the response's body if it is transient???
        // try {
        Representation requestRepresentation = response.getEntity();
        // log.log(logLevel, "Body - (" + requestRepresentation.getText() +
        // ")");
        // } catch (IOException e1) {
        // }
      }
      if (response.getLocationRef() != null) {
        log.log(logLevel, "Location - " + response.getLocationRef());
      }
      if (response.getServerInfo() != null) {
        log.log(logLevel, "ServerInfo - (Address: " + response.getServerInfo().getAddress() + ", Agent: " + response.getServerInfo().getAgent() + ", Port: "
                + response.getServerInfo().getPort() + ")");
      }
    } else {
      log.log(logLevel, "HttpResponse is NULL!");
    }
    log.log(logLevel, "----- End HttpResponse -----");
  }

}
