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

package org.activiti.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;

import com.mongodb.DBObject;


/**
 * @author Tom Baeyens
 */
public class RestCall {

  protected List<String> parameterErrors = null;
  protected HttpServletMethod httpServletMethod;
  protected HttpServletRequest httpServletRequest;
  protected HttpServletResponse httpServletResponse;
  protected Map<String, String> urlVariables;
  protected List<String> choppedPath;
  
  public RestCall(HttpServletMethod httpServletMethod, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    this.httpServletMethod = httpServletMethod;
    this.httpServletRequest = httpServletRequest;
    this.httpServletResponse = httpServletResponse;
  }

  public void sendResponse(DBObject jsonObject) {
    // add some nice formatting
    String json = jsonObject.toString();
    String formattedJson = new JSONObject(json).toString(2);
    sendResponse(formattedJson);
  }
  
  public void sendResponse(List<DBObject> jsonObjects) {
    // add some nice formatting
    String json = jsonObjects.toString();
    String formattedJson = new JSONArray(json).toString(2);
    sendResponse(formattedJson);
  }

  public void sendResponse(String json) {
    httpServletResponse.setContentType("text/json");
    try {
      httpServletResponse
        .getOutputStream()
        .println(json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String getPathInfo() {
    return httpServletRequest.getPathInfo();
  }
  
  public String getAuthenticatedUserId() {
    return Authentication.getAuthenticatedUserId();
  }

  public void setUrlVariables(Map<String, String> urlVariables) {
    this.urlVariables = urlVariables;
  }
  
  public void addParameterError(String msg) {
    if (parameterErrors==null) {
      parameterErrors = new ArrayList<String>();
    }
    parameterErrors.add(msg);
  }
  
  public List<String> getChoppedPath() {
    if (choppedPath==null) {
      choppedPath = new ArrayList<String>();
      StringTokenizer tokenizer = new StringTokenizer(getPathInfo(), "/");
      while (tokenizer.hasMoreTokens()) {
        choppedPath.add(tokenizer.nextToken());
      }
    }
    return choppedPath;
  }
  
  public Map<String, String> getUrlVariables() {
    return urlVariables;
  }

  
  public HttpServletMethod getHttpServletMethod() {
    return httpServletMethod;
  }

  
  public HttpServletRequest getHttpServletRequest() {
    return httpServletRequest;
  }

  
  public HttpServletResponse getHttpServletResponse() {
    return httpServletResponse;
  }
}
