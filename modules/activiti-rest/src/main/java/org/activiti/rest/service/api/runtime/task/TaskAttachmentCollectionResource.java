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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.AttachmentRequest;
import org.activiti.rest.service.api.engine.AttachmentResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class TaskAttachmentCollectionResource extends TaskBaseResource {

  @Get
  public List<AttachmentResponse> getAttachments() {
    if(!authenticate())
      return null;
    
    List<AttachmentResponse> result = new ArrayList<AttachmentResponse>();
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    HistoricTaskInstance task = getHistoricTaskFromRequest();
    
    for(Attachment attachment : ActivitiUtil.getTaskService().getTaskAttachments(task.getId())) {
      result.add(responseFactory.createAttachmentResponse(this, attachment));
    }
    
    return result;
  }
  
  @Post
  public AttachmentResponse createAttachment(Representation representation) {
    if (authenticate() == false)
      return null;
    AttachmentResponse result = null;
    Task task = getTaskFromRequest();
    try {
      if (MediaType.MULTIPART_FORM_DATA.isCompatible(representation.getMediaType())) {
        result = createBinaryAttachment(representation, task);
      } else {
        result = createSimpleAttachment(representation, task);
      }
    } catch (IOException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
    } catch (FileUploadException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
    }
    
    setStatus(Status.SUCCESS_CREATED);
    return result;
  }
  
  
  protected AttachmentResponse createSimpleAttachment(Representation representation, Task task) throws IOException {
    AttachmentRequest req = getConverterService().toObject(representation, AttachmentRequest.class, this);
    if (req.getName() == null) {
      throw new ActivitiIllegalArgumentException("Attachment name is required.");
    }

    Attachment createdAttachment = ActivitiUtil.getTaskService().createAttachment(req.getType(), task.getId(), task.getProcessInstanceId(), req.getName(),
            req.getDescription(), req.getExternalUrl());

    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory().createAttachmentResponse(this, createdAttachment);
  }
  
  protected AttachmentResponse createBinaryAttachment(Representation representation, Task task) throws FileUploadException, IOException {
    RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
    List<FileItem> items = upload.parseRepresentation(representation);
    
    String name = null;
    String description = null;
    String type = null;
    FileItem uploadItem = null;
    
    for (FileItem fileItem : items) {
      if(fileItem.isFormField()) {
        if("name".equals(fileItem.getFieldName())) {
          name = fileItem.getString("UTF-8");
        } else if("description".equals(fileItem.getFieldName())) {
          description = fileItem.getString("UTF-8");
        } else if("type".equals(fileItem.getFieldName())) {
          type = fileItem.getString("UTF-8");
        }
      } else  if(fileItem.getName() != null) {
        uploadItem = fileItem;
      }
    }
    
    if (name == null) {
      throw new ActivitiIllegalArgumentException("Attachment name is required.");
    }
    
    if (uploadItem == null) {
      throw new ActivitiIllegalArgumentException("Attachment content is required.");
    }
    
    Attachment createdAttachment = ActivitiUtil.getTaskService().createAttachment(type, task.getId(), task.getProcessInstanceId(), name,
            description, uploadItem.getInputStream());
    
    setStatus(Status.SUCCESS_CREATED);
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory().createAttachmentResponse(this, createdAttachment);
  }
}
