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


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

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
@Api(tags = { "Tasks" }, description = "Manage Tasks", authorizations = { @Authorization(value = "basicAuth") })
public class TaskAttachmentCollectionResource extends TaskBaseResource {

  @Autowired
  protected ObjectMapper objectMapper;


  @ApiOperation(value = "Get all attachments on a task", tags = {"Tasks"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the task was found and the attachments are returned."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found.")
  })  
  @RequestMapping(value = "/runtime/tasks/{taskId}/attachments", method = RequestMethod.GET, produces = "application/json")
  public List<AttachmentResponse> getAttachments(@ApiParam(name = "taskId", value="The id of the task to get the attachments for.") @PathVariable String taskId, HttpServletRequest request) {
    List<AttachmentResponse> result = new ArrayList<AttachmentResponse>();
    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);

    for (Attachment attachment : taskService.getTaskAttachments(task.getId())) {
      result.add(restResponseFactory.createAttachmentResponse(attachment));
    }

    return result;
  }


  @ApiOperation(value = "Create a new attachment on a task, containing a link to an external resource or an attached file", tags = {"Tasks"},
      notes="## Create a new attachment on a task, containing a link to an external resource\n\n"
          + " ```JSON\n" + "{\n" + "  \"name\":\"Simple attachment\",\n" + "  \"description\":\"Simple attachment description\",\n"
          + "  \"type\":\"simpleType\",\n" + "  \"externalUrl\":\"http://activiti.org\"\n" + "} ```"
          + "\n\n\n"
          + "Only the attachment name is required to create a new attachment.\n"
          + "\n\n\n"
          + "## Create a new attachment on a task, with an attached file\n\n"
          + "The request should be of type multipart/form-data. There should be a single file-part included with the binary value of the variable. On top of that, the following additional form-fields can be present:\n"
          + "\n"
          + "- *name*: Required name of the variable.\n" + "\n"
          + "- *description*: Description of the attachment, optional.\n" + "\n"
          + "- *type*: Type of attachment, optional. Supports any arbitrary string or a valid HTTP content-type."
      )
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indicates the attachment was created and the result is returned."),
      @ApiResponse(code = 400, message = "Indicates the attachment name is missing from the request."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found.")
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}/attachments", method = RequestMethod.POST, produces = "application/json")
  public AttachmentResponse createAttachment(@ApiParam(name = "taskId", value="The id of the task to create the attachment for.") @PathVariable String taskId, HttpServletRequest request, HttpServletResponse response) {

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

    Attachment createdAttachment = taskService.createAttachment(attachmentRequest.getType(), task.getId(), task.getProcessInstanceId(), attachmentRequest.getName(),
        attachmentRequest.getDescription(), attachmentRequest.getExternalUrl());

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
      Attachment createdAttachment = taskService.createAttachment(type, task.getId(), task.getProcessInstanceId(), name, description, file.getInputStream());

      response.setStatus(HttpStatus.CREATED.value());
      return restResponseFactory.createAttachmentResponse(createdAttachment);

    } catch (Exception e) {
      throw new ActivitiException("Error creating attachment response", e);
    }
  }
}
