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

package org.activiti.rest.service.api.identity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.rest.common.api.ActivitiUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class UserPictureResource extends BaseUserResource {

  @Get
  public InputRepresentation getUserPicture() {
    if(!authenticate())
      return null;
    
    User user = getUserFromRequest();
    Picture userPicture = ActivitiUtil.getIdentityService().getUserPicture(user.getId());
    
    if(userPicture == null) {
      throw new ActivitiObjectNotFoundException("The user with id '" + user.getId() + "' does not have a picture.", Picture.class);
    }
    
    MediaType mediaType = MediaType.IMAGE_JPEG;
    if(userPicture.getMimeType() != null) {
      mediaType = MediaType.valueOf(userPicture.getMimeType());
    }
    
    return new InputRepresentation(userPicture.getInputStream(), mediaType);
  }
  
  @Put
  public void updateUserPicture(Representation representation) {
  	if(authenticate() == false) return;
    User user = getUserFromRequest();
    
    if(!MediaType.MULTIPART_FORM_DATA.isCompatible(representation.getMediaType())) {
      throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(), "The request should be of type 'multipart/form-data'.", null, null);
    }
    
    RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
    try {
      FileItem uploadItem = null;
      List<FileItem> items = upload.parseRepresentation(representation);
      String mimeType = MediaType.IMAGE_JPEG.toString();
      
      for (FileItem fileItem : items) {
        if(fileItem.isFormField()) {
          if("mimeType".equals(fileItem.getFieldName())) {
            mimeType = fileItem.getString("UTF-8");
          }
        } else  if(fileItem.getName() != null) {
          uploadItem = fileItem;
        }
      }
      
      if(uploadItem == null) {
        throw new ActivitiIllegalArgumentException("No file content was found in request body.");
      }
      
      int size = ((Long) uploadItem.getSize()).intValue();
      
      // Copy file-body in a bytearray as the engine requires this
      ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream(size);
      IOUtils.copy(uploadItem.getInputStream(), bytesOutput);
      
      Picture newPicture = new Picture(bytesOutput.toByteArray(), mimeType);
      ActivitiUtil.getIdentityService().setUserPicture(user.getId(), newPicture);
      
    } catch (FileUploadException e) {
      throw new ActivitiException("Error with uploaded file: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new ActivitiException("Error while reading uploaded file: " + e.getMessage(), e);
    }
  }
}
