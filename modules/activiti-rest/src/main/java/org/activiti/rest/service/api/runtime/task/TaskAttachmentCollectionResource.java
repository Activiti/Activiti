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

package org.activiti.rest.service.api.runtime.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.engine.AttachmentRequest;
import org.activiti.rest.service.api.engine.AttachmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Frederik Heremans
 */
@RestController
public class TaskAttachmentCollectionResource extends TaskBaseResource {
  
  @Autowired
  protected ObjectMapper objectMapper;

  @RequestMapping(value="/runtime/tasks/{taskId}/attachments", method = RequestMethod.GET, produces="application/json")
  public List<AttachmentResponse> getAttachments(@PathVariable String taskId, HttpServletRequest request) {
    List<AttachmentResponse> result = new ArrayList<AttachmentResponse>();
    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
    
    for (Attachment attachment : taskService.getTaskAttachments(task.getId())) {
      result.add(restResponseFactory.createAttachmentResponse(attachment));
    }
    
    return result;
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/attachments", method = RequestMethod.POST, produces="application/json")
  public AttachmentResponse createAttachment(@PathVariable String taskId, HttpServletRequest request, HttpServletResponse response) {
    
    AttachmentResponse result = null;
    Task task = getTaskFromRequest(taskId);
    if (request instanceof MultipartHttpServletRequest) {
      result = createBinaryAttachment((MultipartHttpServletRequest) request, task, response);
    } else {
      
      AttachmentRequest attachmentRequest = null;
      try {
        attachmentRequest = objectMapper.readValue(request.getInputStream(), AttachmentRequest.class);
        
      } catch (Exception e) {
        throw new ActivitiIllegalArgumentException("Failed to serialize to a AttachmentRequest instance", e);
      }
      
      if (attachmentRequest == null) {
        throw new ActivitiIllegalArgumentException("AttachmentRequest properties not found in request");
      }
      
      result = createSimpleAttachment(attachmentRequest, task);
    }
    
    response.setStatus(HttpStatus.CREATED.value());
    return result;
  }
  
  protected AttachmentResponse createSimpleAttachment(AttachmentRequest attachmentRequest, Task task) {
    
    if (attachmentRequest.getName() == null) {
      throw new ActivitiIllegalArgumentException("Attachment name is required.");
    }

    Attachment createdAttachment = taskService.createAttachment(attachmentRequest.getType(), task.getId(), 
        task.getProcessInstanceId(), attachmentRequest.getName(), attachmentRequest.getDescription(), attachmentRequest.getExternalUrl());

    return restResponseFactory.createAttachmentResponse(createdAttachment);
  }
  
  protected AttachmentResponse createBinaryAttachment(MultipartHttpServletRequest request, Task task, HttpServletResponse response) {
    
    String name = null;
    String description = null;
    String type = null;
    
    Map<String, String[]> paramMap = request.getParameterMap();
    for (String parameterName : paramMap.keySet()) {
      if (paramMap.get(parameterName).length > 0) {
        
        if (parameterName.equalsIgnoreCase("name")) {
          name = paramMap.get(parameterName)[0];
          
        } else if (parameterName.equalsIgnoreCase("description")) {
          description = paramMap.get(parameterName)[0];
          
        } else if (parameterName.equalsIgnoreCase("type")) {
          type = paramMap.get(parameterName)[0];
        }
      }
    }
    
    if (name == null) {
      throw new ActivitiIllegalArgumentException("Attachment name is required.");
    }
    
    if (request.getFileMap().size() == 0) {
      throw new ActivitiIllegalArgumentException("Attachment content is required.");
    }
    
    MultipartFile file = request.getFileMap().values().iterator().next();
    
    if (file == null) {
      throw new ActivitiIllegalArgumentException("Attachment content is required.");
    }
    
    try {
      Attachment createdAttachment = taskService.createAttachment(type, task.getId(), task.getProcessInstanceId(), name,
              description, file.getInputStream());
      
      response.setStatus(HttpStatus.CREATED.value());
      return restResponseFactory.createAttachmentResponse(createdAttachment);
      
    } catch (Exception e) {
      throw new ActivitiException("Error creating attachment response", e);
    }
  }
}
