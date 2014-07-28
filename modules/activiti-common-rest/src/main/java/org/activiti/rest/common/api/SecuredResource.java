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

package org.activiti.rest.common.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.rest.common.application.ActivitiRestApplication;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

/**
 * @author Tijs Rademakers
 * @author Frederik Heremans
 */
public class SecuredResource extends ServerResource {

  protected static final String USER = "user";
  protected static final String ADMIN = "admin";
  protected ISO8601DateFormat isoFormatter = new ISO8601DateFormat();
  protected String loggedInUser;
  
  /**
   * Create a full URL to a rest-resource.
   * 
   * @param urlFragments fragments of the URL, relative to the base of the rest-application.
   * @param arguments arguments to replace the placeholders in the urlFragments, using the {@link MessageFormat}
   * conventions (eg. <code>{0}</code> is replaced by first argument value).
   */
  public String createFullResourceUrl(String[] urlFragments, Object ... arguments) {
    Reference url = getRequest().getRootRef().clone();
    for(String urlFragment : urlFragments) {
      url.addSegment(MessageFormat.format(urlFragment, arguments));
    }
    return url.toString();
  }
  
  /**
   * @return the {@link MediaType} resolved from the given resource-name. Returns null if the type cannot
   * be resolved based on the given name.
   */
  public MediaType resolveMediaType(String resourceName) {
    return ((ActivitiRestApplication)getApplication()).getMediaTypeResolver().resolveMediaType(resourceName);
  }
  
  /**
   * @return the Restlet Application, casted to the given type.
   */
  @SuppressWarnings("unchecked")
  public <T extends ActivitiRestApplication> T getApplication(Class<T> applicationClass) {
    return (T) getApplication();
  }
  
  /**
   * Get a request attribute value, decoded. 
   */
  public String getAttribute(String name) {
    return decode((String) getRequest().getAttributes().get(name));
  }
  
  /**
   * @return the value for the given query-parameter name. 
   * Returns null, if the query-parameter was not set.
   */
  protected String getQueryParameter(String name, Form query) {
    return query.getFirstValue(name);
  }
  
  /**
   * @return the value for the given query-parameter name, as an integer value. 
   * Returns null, if the query-parameter was not set.
   * 
   * @throws ActivitiIllegalArgumentException when the query parameter is set but has cannot be converted to an integer
   */
  protected Integer getQueryParameterAsInt(String name, Form query) {
    Integer result = null;
    String stringValue = getQueryParameter(name, query);
    if(stringValue != null) {
      try {
        result = Integer.parseInt(stringValue);
      } catch(NumberFormatException nfe) {
        throw new ActivitiIllegalArgumentException("The given value for query-parameter '" + name + "' is not an integer: " + stringValue);
      }
    }
    return result;
  }
  
  /**
   * @return the value for the given query-parameter name, as an boolean value. 
   * Returns null, if the query-parameter was not set.
   * 
   * @throws ActivitiIllegalArgumentException when the query parameter is set but has cannot be converted to a boolean
   */
  protected Boolean getQueryParameterAsBoolean(String name, Form query) {
    String stringValue = getQueryParameter(name, query);
    if(stringValue != null) {
      if(Boolean.TRUE.toString().equals(stringValue.toLowerCase())) {
        return Boolean.TRUE;
      } else if(Boolean.FALSE.toString().equals(stringValue.toLowerCase())) {
        return Boolean.FALSE;
      } else {
        throw new ActivitiIllegalArgumentException("The given value for query-parameter '" + name + "' should be one fo 'true' or 'false', instead of: " + stringValue);
      }
    }
    
    return null;
  }
  
  /**
   * @return the value for the given query-parameter name, as a date value. 
   * Returns null, if the query-parameter was not set.
   * 
   * @throws ActivitiIllegalArgumentException when the query parameter is set but has cannot be converted to a date
   */
  protected Date getQueryParameterAsDate(String name, Form query) {
    Date result = null;
    String stringValue = getQueryParameter(name, query);
    if(stringValue != null) {
      try {
        result = isoFormatter.parse(stringValue);
      } catch (ParseException e) {
        throw new ActivitiIllegalArgumentException("The given value for query-parameter '" + name + "' is not a valid date: " + stringValue, e);
      } catch (IllegalArgumentException e) {
        throw new ActivitiIllegalArgumentException("The given value for query-parameter '" + name + "' is not a valid date: " + stringValue, e);
      }
    }
    return result;
  }
  
  protected String decode(String string) {
    if(string != null) {
      try {
        return URLDecoder.decode(string, "UTF-8");
      } catch (UnsupportedEncodingException uee) {
        throw new IllegalStateException("JVM does not support UTF-8 encoding.", uee);
      }
    }
    return null;
  }
  
  protected boolean authenticate() {
    return authenticate(null);
  }
  
  protected boolean authenticate(String group) {
    loggedInUser = ((ActivitiRestApplication) getApplication()).authenticate(getRequest(), getResponse());
    if(loggedInUser == null) {
      // Not authenticated
      setStatus(getAuthenticationFailureStatus(), "Authentication is required");
      return false;
    
    } else if(group == null) {
      ActivitiUtil.getIdentityService().setAuthenticatedUserId(loggedInUser);
      return true;
    
    } else {
      boolean allowed = false;
      List<Group> groupList = ActivitiUtil.getIdentityService().createGroupQuery().groupMember(loggedInUser).list();
      if(groupList != null) {
        for (Group groupObject : groupList) {
          if(groupObject.getId().equals(group)) {
            allowed = true;
            ActivitiUtil.getIdentityService().setAuthenticatedUserId(loggedInUser);
            break;
          }
        }
      }
      if(allowed == false) {
        setStatus(getAuthenticationFailureStatus(), "User is not part of the group " + group);
      }
      return allowed;
    }
  }
  
  protected Map<String, Object> retrieveVariables(JsonNode jsonNode) {
    Map<String, Object> variables = new HashMap<String, Object>();
    if (jsonNode != null) {
      Iterator<String> itName = jsonNode.fieldNames();
      while(itName.hasNext()) {
        String name = itName.next();
        JsonNode valueNode = jsonNode.path(name);
        if (valueNode.isBoolean()) {
          variables.put(name, valueNode.booleanValue());
        } else if (valueNode.isInt()) {
          variables.put(name, valueNode.intValue());
        } else if (valueNode.isLong()) {
          variables.put(name, valueNode.longValue());
        } else if (valueNode.isDouble()) {
          variables.put(name, valueNode.doubleValue());
        } else if (valueNode.isTextual()) {
          variables.put(name, valueNode.textValue());
        } else {
          // Not using asText() due to the fact we expect a null-value to be returned rather than en emtpy string
          // when node is not a simple value-node
          variables.put(name, valueNode.textValue());
        }
      }
    }
    return variables;
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_UNAUTHORIZED;
  }
}
