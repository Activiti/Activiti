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

package org.activiti.rest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.rest.handler.TasksHandler;
import org.activiti.rest.impl.BadRequestException;
import org.activiti.rest.impl.HttpServletMethod;
import org.activiti.rest.impl.Parameter;
import org.activiti.rest.impl.RestCall;
import org.activiti.rest.impl.RestException;
import org.activiti.rest.impl.RestHandler;
import org.activiti.rest.impl.UrlMatcher;
import org.apache.commons.codec.binary.Base64;


/**
 * @author Tom Baeyens
 */
public class RestServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(RestServlet.class.getName());

  protected static Map<String, RestHandler> staticHandlers = new HashMap<String, RestHandler>();
  protected static List<UrlMatcher> dynamicHandlers = new ArrayList<UrlMatcher>();

  private String processEngineName = ProcessEngines.NAME_DEFAULT;
  
  public RestServlet() { 
    register(new TasksHandler());
  }

  public void register(RestHandler handler) {
    String urlPattern = handler.getUrlPattern();
    if (urlPattern.indexOf('{')==-1) {
      staticHandlers.put(urlPattern, handler);
    } else {
      dynamicHandlers.add(new UrlMatcher(handler));
    }
    handler.setRestServlet(this);
  }

  public ProcessEngine getProcessEngine() {
    return ProcessEngines.getProcessEngine(processEngineName);
  }

  public void handle(RestCall restCall) {
    RestHandler staticHandler = getRestHandler(restCall);
    staticHandler.handle(restCall);
  }

  private RestHandler getRestHandler(RestCall restCall) {
    String pathInfo = restCall.getPathInfo();
    RestHandler staticHandler = staticHandlers.get(pathInfo);
    if (staticHandler==null) {
      Iterator<UrlMatcher> iter = dynamicHandlers.iterator();
      RestHandler restHandler = null;
      while (restHandler==null && iter.hasNext()) {
        UrlMatcher urlMatcher = iter.next();
        Map<String, String> urlVariables = urlMatcher.matches(restCall);
        if (urlVariables!=null) {
          restCall.setUrlVariables(urlVariables);
          restHandler = urlMatcher.getRestHandler();
        }
      }
      if (restHandler==null) {
        throw new BadRequestException("invalid url "+pathInfo);
      }
    }
    return staticHandler;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    authenticateAndHandle(HttpServletMethod.GET, request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    authenticateAndHandle(HttpServletMethod.POST, request, response);
  }

  protected void authenticateAndHandle(HttpServletMethod method, HttpServletRequest request, HttpServletResponse response) {
    if ("/help".equals(request.getPathInfo())) {
      sendHelp(response);
      return;
    }
    try {
      String authenticatedUserId = authenticate(request);
      Authentication.setAuthenticatedUserId(authenticatedUserId);
      handle(new RestCall(method, request, response));
      
    } catch (RestException e) {
      log.log(Level.SEVERE, "exception while processing "+request.getPathInfo()+" : "+e.getMessage(), e);
      sendError(response, e.getResponseCode(), e.getMessage());
      
    } finally {
      Authentication.setAuthenticatedUserId(null);
    }
  }

  protected void sendHelp(HttpServletResponse response) {
    response.setContentType("text/plain");
    try {
      Map<String, RestHandler> restHandlers = new HashMap<String, RestHandler>();
      restHandlers.putAll(staticHandlers);
      for (UrlMatcher urlMatcher: dynamicHandlers) {
        RestHandler restHandler = urlMatcher.getRestHandler();
        restHandlers.put(restHandler.getUrlPattern(), restHandler);
      }
      SortedSet<String> urlPatterns = new TreeSet<String>(restHandlers.keySet());
      ServletOutputStream out = response.getOutputStream();
      for (String urlPattern: urlPatterns) {
        RestHandler restHandler = restHandlers.get(urlPattern);
        String parametersTemplate = "";
        List<String> parameterDescriptions = new ArrayList<String>();
        for (Field handlerField: restHandler.getClass().getDeclaredFields()) {
          if (Parameter.class.isAssignableFrom(handlerField.getType())) {
            handlerField.setAccessible(true);
            Parameter<?> parameter = (Parameter<?>) handlerField.get(restHandler);
            String parameterName = parameter.getName();
            if (parametersTemplate.length()==0) {
              parametersTemplate += "?";
            } else {
              parametersTemplate += "&";
            }
            if (parameter.isRequired()) {
              parametersTemplate += parameterName+"={"+parameterName+"}";
            } else {
              parametersTemplate += "["+parameterName+"={"+parameterName+"}]";
            }
            parameterDescriptions.add("  * "+parameterName+(parameter.isRequired() ? " [required] " : " [optional] ")+parameter.getTypeDescription()+"\n"+
                                      "    "+parameter.getDescription());
          }
        }
        out.println(restHandler.getMethod().toString()+" "+urlPattern+parametersTemplate);
        for (String parameterDescription: parameterDescriptions) {
          out.println(parameterDescription);
        }
        
        out.println();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** @return userId if authentication succeeds
   * @throws RestException if authentication fails */
  protected String authenticate(HttpServletRequest request) {
    String headerText = request.getHeader("Auth");
    if ( headerText!=null 
         && headerText.startsWith("Basic ") 
       ) {
      String encodedHeader = headerText.substring(6);
      String decodedHeader = new String(Base64.decodeBase64(encodedHeader));
      int colonIndex = decodedHeader.indexOf(':');
      if (colonIndex!=-1) {
        String username = decodedHeader.substring(0, colonIndex);
        String password = decodedHeader.substring(colonIndex+1);
        
        boolean authenticationOk = getProcessEngine()
          .getIdentityService()
          .checkPassword(username, password);
        
        if (authenticationOk) {
          return username; 
        }
      }
    }
    throw new RestException(HttpServletResponse.SC_UNAUTHORIZED, "basic authentication required");
  }

  protected void sendError(HttpServletResponse response, int responseCode, String errorMessage) {
    try {
      response.sendError(responseCode, errorMessage);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
