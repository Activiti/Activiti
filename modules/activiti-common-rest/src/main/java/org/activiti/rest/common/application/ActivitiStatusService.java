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

package org.activiti.rest.common.application;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.rest.common.api.RestError;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.service.StatusService;

import com.fasterxml.jackson.databind.JsonMappingException;


/**
 * Custom {@link StatusService}, translating dedicated {@link ActivitiException}'s into HTTP-response
 * codes.
 * 
 * @author Frederik Heremans
 */
public class ActivitiStatusService extends StatusService {

  
  protected static final String NEWLINE_REPLACE_REGEX = "\\r\\n|\\r|\\n";
  
  
  /**
   * Overriding this method to return a JSON-object representing the error that occurred instead of
   * the default HTML body Restlet provides.
   */
  @Override
  public Representation getRepresentation(Status status, Request request, Response response) {
    if(status != null && status.isError()) {
      RestError error = new RestError();
      error.setStatusCode(status.getCode());
      error.setErrorMessage(status.getDescription());
      return new JacksonRepresentation<RestError>(error);
    } else {
      return super.getRepresentation(status, request, response);
    }
  }
  
  
  
  @Override
  public Status getStatus(Throwable throwable, Request request, Response response) {
    Status status = null;
    if (throwable instanceof JsonMappingException && throwable.getCause() != null) {
      // Possible that the Jackson-unmarchalling has a more specific cause. if no specific exception caused
      // the throwable, it will be handled as a normal exception
      status = getSpecificStatus(throwable.getCause(), request, response);
    }
    
    if(status == null) {
      Throwable causeThrowable = null;
      if (throwable.getCause() != null && throwable.getCause() instanceof ActivitiException) {
        causeThrowable = throwable.getCause();
      } else {
        causeThrowable = throwable;
      }
      status = getSpecificStatus(causeThrowable, request, response);
    }
    return status != null ? status : Status.SERVER_ERROR_INTERNAL;
  }
  
  protected Status getSpecificStatus(Throwable throwable, Request request, Response response) {
    Status status = null;
    
    if(throwable instanceof ActivitiObjectNotFoundException) {
      // 404 - Entity not found
      status = new Status(Status.CLIENT_ERROR_NOT_FOUND.getCode(), getSafeStatusName(throwable.getMessage()), getSafeStatusName(throwable.getMessage()), null);
    } else if(throwable instanceof ActivitiIllegalArgumentException) {
      // 400 - Bad Request
      status = new Status(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), getSafeStatusName(throwable.getMessage()), getSafeStatusName(throwable.getMessage()), null);
    } else if (throwable instanceof ActivitiOptimisticLockingException || throwable instanceof ActivitiTaskAlreadyClaimedException) {
      // 409 - Conflict
      status = new Status(Status.CLIENT_ERROR_CONFLICT.getCode(), getSafeStatusName(throwable.getMessage()), getSafeStatusName(throwable.getMessage()), null);
    }  else if (throwable instanceof ResourceException) {
      ResourceException re = (ResourceException) throwable;
      status = re.getStatus();
    } else if(throwable instanceof ActivitiException) {
      status = new Status(Status.SERVER_ERROR_INTERNAL.getCode(), getSafeStatusName(throwable.getMessage()), getSafeStatusName(throwable.getMessage()), null);;
    } else {
      status = null;
    }
    
    return status;
  }
  
  protected String getSafeStatusName(String name) {
    if(name != null) {
      return name.replaceAll(NEWLINE_REPLACE_REGEX, " ");
    }
    return null;
  }
}
