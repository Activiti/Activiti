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

package org.activiti.rest.api.repository;

import java.util.List;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

/**
 * @author Tijs Rademakers
 */
public class DeploymentUploadResource extends SecuredResource {
  
  @Post
  public DeploymentResponse uploadDeployment(Representation entity) {
    try {
      if(authenticate(SecuredResource.ADMIN) == false) return null;
      
      RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
      List<FileItem> items = upload.parseRepresentation(entity);
      
      FileItem uploadItem = null;
      for (FileItem fileItem : items) {
        if(fileItem.getName() != null) {
          uploadItem = fileItem;
        }
      }
      
      DeploymentBuilder deploymentBuilder = ActivitiUtil.getRepositoryService().createDeployment();
      String fileName = uploadItem.getName();
      if (fileName.endsWith(".bpmn20.xml")) {
        deploymentBuilder.addInputStream(fileName, uploadItem.getInputStream());
      } else if (fileName.toLowerCase().endsWith(".bar") || fileName.toLowerCase().endsWith(".zip")) {
        deploymentBuilder.addZipInputStream(new ZipInputStream(uploadItem.getInputStream()));
      } else {
        throw new ActivitiException("File must be of type .bpmn20.xml, .bar or .zip");
      }
      deploymentBuilder.name(fileName);
      Deployment deployment = deploymentBuilder.deploy();
      return new DeploymentResponse(deployment);
      
    } catch (Exception e) {
      throw new ActivitiException(e.getMessage(), e);
    }
  }
}
