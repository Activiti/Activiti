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

package org.activiti.rest.service.api.form;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.form.FormData;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * @author Tijs Rademakers
 */
public class FormDataResource extends SecuredResource {

  @Get
  public FormDataResponse getFormData() {
    if (authenticate() == false)
      return null;

    Form urlQuery = getQuery();
    
    String taskId = getQueryParameter("taskId", urlQuery);
    String processDefinitionId = getQueryParameter("processDefinitionId", urlQuery);
    
    if (taskId == null && processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("The taskId or processDefinitionId parameter has to be provided");
    }
    
    if (taskId != null && processDefinitionId != null) {
      throw new ActivitiIllegalArgumentException("Not both a taskId and a processDefinitionId parameter can be provided");
    }
    
    FormData formData = null;
    String id = null;
    if (taskId != null) {
      formData = ActivitiUtil.getFormService().getTaskFormData(taskId);
      id = taskId;
    } else {
      formData = ActivitiUtil.getFormService().getStartFormData(processDefinitionId);
      id = processDefinitionId;
    }
    
    if (formData == null) {
      throw new ActivitiObjectNotFoundException("Could not find a form data with id '" + id + "'.", FormData.class);
    }
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
        .createFormDataResponse(this, formData);
  }
  
  @Post
  public ProcessInstanceResponse submitForm(SubmitFormRequest submitRequest) {
    if (!authenticate()) {
      return null;
    }

    if (submitRequest == null) {
      throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(), 
          "A request body was expected when executing the form submit.", null, null));
    }

    if (submitRequest.getTaskId() == null && submitRequest.getProcessDefinitionId() == null) {
      throw new ActivitiIllegalArgumentException("The taskId or processDefinitionId property has to be provided");
    }
    
    Map<String, String> propertyMap = new HashMap<String, String>();
    if (submitRequest.getProperties() != null) {
      for (RestFormProperty formProperty : submitRequest.getProperties()) {
        propertyMap.put(formProperty.getId(), formProperty.getValue());
      }
    }
    
    if (submitRequest.getTaskId() != null) {
      ActivitiUtil.getFormService().submitTaskFormData(submitRequest.getTaskId(), propertyMap);
      return null;
    } else {
      ProcessInstance processInstance = null;
      if (submitRequest.getBusinessKey() != null) {
        processInstance = ActivitiUtil.getFormService().submitStartFormData(submitRequest.getProcessDefinitionId(), submitRequest.getBusinessKey(), propertyMap);
      } else {
        processInstance = ActivitiUtil.getFormService().submitStartFormData(submitRequest.getProcessDefinitionId(), propertyMap);
      }
      return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
          .createProcessInstanceResponse(this, processInstance);
    }
  }
}
