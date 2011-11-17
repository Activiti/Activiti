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

package org.activiti.rest.api.task;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Attachment;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

/**
 * @author Tijs Rademakers
 */
public class TaskAttachmentAddResource extends SecuredResource {
  
  @Put
  public AttachmentResponse addAttachment(Representation entity) {
    if(authenticate() == false) return null;
    
    String taskId = (String) getRequest().getAttributes().get("taskId");
    
    try {
      RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
      List<FileItem> items = upload.parseRepresentation(entity);
      
      FileItem uploadItem = null;
      for (FileItem fileItem : items) {
        if(fileItem.getName() != null) {
          uploadItem = fileItem;
        }
      }
      
      String fileName = uploadItem.getName();
      
      Attachment attachment = ActivitiUtil.getTaskService().createAttachment(
          uploadItem.getContentType(), taskId, null, fileName, fileName, uploadItem.getInputStream());
      
      return new AttachmentResponse(attachment);
      
    } catch(Exception e) {
      throw new ActivitiException("Unable to add new attachment to task " + taskId);
    }
  }
}
