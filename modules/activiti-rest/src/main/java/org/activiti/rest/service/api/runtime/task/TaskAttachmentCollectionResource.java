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
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author Frederik Heremans
 */
@RestController
public class TaskAttachmentCollectionResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}/attachments", method = RequestMethod.GET, produces="application/json")
  public List<AttachmentResponse> getAttachments(@PathVariable String taskId, HttpServletRequest request) {
    List<AttachmentResponse> result = new ArrayList<AttachmentResponse>();
    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
    
    String serverRootUrl = request.getRequestURL().toString();
    serverRootUrl = serverRootUrl.substring(0, serverRootUrl.indexOf("/runtime/tasks/"));
    
    for (Attachment attachment : taskService.getTaskAttachments(task.getId())) {
      result.add(restResponseFactory.createAttachmentResponse(attachment, serverRootUrl));
    }
    
    return result;
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/attachments", method = RequestMethod.POST, produces="application/json")
  public AttachmentResponse createAttachment(@PathVariable String taskId, @RequestParam Map<String,String> allRequestParams,
      @RequestParam("file") MultipartFile file, @RequestBody AttachmentRequest attachmentRequest, 
      HttpServletRequest request, HttpServletResponse response) {
    
    String serverRootUrl = request.getRequestURL().toString();
    serverRootUrl = serverRootUrl.substring(0, serverRootUrl.indexOf("/runtime/tasks/"));
    
    AttachmentResponse result = null;
    Task task = getTaskFromRequest(taskId);
    if (file != null) {
      result = createBinaryAttachment(file, allRequestParams, task, serverRootUrl, response);
    } else {
      result = createSimpleAttachment(attachmentRequest, task, serverRootUrl);
    }
    
    response.setStatus(HttpStatus.SC_CREATED);
    return result;
  }
  
  protected AttachmentResponse createSimpleAttachment(AttachmentRequest attachmentRequest, 
      Task task, String serverRootUrl) {
    
    if (attachmentRequest.getName() == null) {
      throw new ActivitiIllegalArgumentException("Attachment name is required.");
    }

    Attachment createdAttachment = taskService.createAttachment(attachmentRequest.getType(), task.getId(), 
        task.getProcessInstanceId(), attachmentRequest.getName(), attachmentRequest.getDescription(), attachmentRequest.getExternalUrl());

    return restResponseFactory.createAttachmentResponse(createdAttachment, serverRootUrl);
  }
  
  protected AttachmentResponse createBinaryAttachment(MultipartFile file, Map<String,String> requestParams, 
      Task task, String serverRootUrl, HttpServletResponse response) {
    
    String name = null;
    String description = null;
    String type = null;
    
    if (requestParams.containsKey("name")) {
      name = requestParams.get("name");
    } else if (requestParams.containsKey("description")) {
      description = requestParams.get("description");
    } else if (requestParams.containsKey("type")) {
      type = requestParams.get("type");
    }
    
    if (name == null) {
      throw new ActivitiIllegalArgumentException("Attachment name is required.");
    }
    
    if (file == null) {
      throw new ActivitiIllegalArgumentException("Attachment content is required.");
    }
    
    try {
      Attachment createdAttachment = taskService.createAttachment(type, task.getId(), task.getProcessInstanceId(), name,
              description, file.getInputStream());
      
      response.setStatus(HttpStatus.SC_CREATED);
      return restResponseFactory.createAttachmentResponse(createdAttachment, serverRootUrl);
      
    } catch (Exception e) {
      throw new ActivitiException("Error creating attachment response", e);
    }
  }
}
