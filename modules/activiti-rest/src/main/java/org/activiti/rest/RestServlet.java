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

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.persistence.Persistence;
import org.activiti.persistence.entity.User;
import org.activiti.rest.handler.TaskHandler;
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

  public RestServlet() { 
    register(new TasksHandler());
    register(new TaskHandler());
  }

  public void register(RestHandler handler) {
    String urlPattern = handler.getUrlPattern();
    if (urlPattern.indexOf('{')==-1) {
      staticHandlers.put(urlPattern, handler);
    } else {
      dynamicHandlers.add(new UrlMatcher(handler));
    }
  }

  public void handle(RestCall restCall) {
    RestHandler staticHandler = getRestHandler(restCall);
    staticHandler.handle(restCall);
  }

  private RestHandler getRestHandler(RestCall restCall) {
    String pathInfo = restCall.getPathInfo();
    RestHandler restHandler = staticHandlers.get(pathInfo);
    if (restHandler==null) {
      Iterator<UrlMatcher> iter = dynamicHandlers.iterator();
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
    return restHandler;
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
      RestCall call = new RestCall(method, request, response);
      String authenticatedUserId = authenticate(call);
      Authentication.setAuthenticatedUserId(authenticatedUserId);
      handle(call);
      
    } catch (RestException e) {
      log.log(Level.SEVERE, "exception while processing "+request.getPathInfo()+" : "+e.getMessage(), e);
      sendError(response, e.getResponseCode(), e.getMessage());
      
    } finally {
      Authentication.setAuthenticatedUserId(null);
    }
  }

  /** @return userId if authentication succeeds
   * @throws RestException if authentication fails */
  protected String authenticate(RestCall call) {
    String headerText = call.getHttpServletRequest().getHeader("Auth");
    if (headerText==null) {
      headerText = call.getHttpServletRequest().getHeader("Authorization");
    }
    if ( headerText!=null 
         && headerText.startsWith("Basic ") 
       ) {
      String encodedHeader = headerText.substring(6);
      String decodedHeader = new String(Base64.decodeBase64(encodedHeader));
      int colonIndex = decodedHeader.indexOf(':');
      if (colonIndex!=-1) {
        String username = decodedHeader.substring(0, colonIndex);
        String password = decodedHeader.substring(colonIndex+1);
        
        User user = Persistence
          .getUsers()
          .findUser(username);
        
        boolean authenticationOk = password.equals(user.getPassword());
        
        if (authenticationOk) {
          return username; 
        }
      }
    }
    
    call.getHttpServletResponse().setHeader("WWW-Authenticate", "Basic realm=\"Activiti\"");
    throw new RestException(HttpServletResponse.SC_UNAUTHORIZED, "basic authentication required");
  }

  protected void sendError(HttpServletResponse response, int responseCode, String errorMessage) {
    try {
      response.sendError(responseCode, errorMessage);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void sendHelp(HttpServletResponse response) {
    response.setContentType("text/html");
    try {
      ServletOutputStream out = response.getOutputStream();
      out.println("<html><body>");
      out.println("<h1>Activiti Rest Interface</h1>");
      out.println("<p>This page documents the rest interface of Activiti</p>");
      
      Map<String, RestHandler> restHandlers = new HashMap<String, RestHandler>();
      restHandlers.putAll(staticHandlers);
      for (UrlMatcher urlMatcher: dynamicHandlers) {
        RestHandler restHandler = urlMatcher.getRestHandler();
        restHandlers.put(restHandler.getUrlPattern(), restHandler);
      }
      SortedSet<String> urlPatterns = new TreeSet<String>(restHandlers.keySet());
      for (String urlPattern: urlPatterns) {
        RestHandler restHandler = restHandlers.get(urlPattern);
        String parametersTemplate = "";
        List<String> parameterDescriptions = new ArrayList<String>();
        for (Field handlerField: restHandler.getClass().getDeclaredFields()) {
          if (Parameter.class.isAssignableFrom(handlerField.getType())) {
            handlerField.setAccessible(true);
            Parameter<?> parameter = (Parameter<?>) handlerField.get(restHandler);
            String parameterName = parameter.getName();
            if (parameter.isParameter()) {
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
            }
            StringBuilder parameterDescription = new StringBuilder();
            parameterDescription.append("  <li><b><code>"+parameterName+"</code></b> ");
            if (parameter.isParameter()) {
              parameterDescription.append(": parameter \n");
            } else {
              parameterDescription.append(": url part variable \n");
            }
            parameterDescription.append("    <ul> \n");
            parameterDescription.append("      <li>"+parameter.getDescription()+"</li> \n");
            parameterDescription.append("      <li>"+(parameter.isRequired() ? "required" : "optional")+"</li> \n");
            parameterDescription.append("      <li>"+parameter.getTypeDescription()+"</li> \n");
            parameterDescription.append("    </ul> \n");
            parameterDescription.append("  </li> \n");
            parameterDescriptions.add(parameterDescription.toString());
          }
        }
        
        out.print("<h3 style='margin-bottom:5px;'><code>");
        out.print(restHandler.getMethod().toString()+" "+urlPattern+parametersTemplate);
        out.println("</code></h3>");
        out.println("  <ul style='margin-top:5px;'>");
        
        for (String parameterDescription: parameterDescriptions) {
          out.print(parameterDescription);
        }
        
        out.println("  </ul>");
        
        out.println("</body></html>");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
