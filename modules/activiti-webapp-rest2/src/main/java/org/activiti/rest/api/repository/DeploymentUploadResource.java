package org.activiti.rest.api.repository;

import java.util.List;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.MediaType;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

public class DeploymentUploadResource extends SecuredResource {
  
  @Post
  public void uploadDeployment(Representation entity) {
    try {
      if(authenticate(SecuredResource.ADMIN) == false) return;
      
      RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
      List<FileItem> items = upload.parseRepresentation(entity);
      
      FileItem uploadItem = null;
      String successMethod = null;
      for (FileItem fileItem : items) {
        if(fileItem.getName() != null) {
          uploadItem = fileItem;
        } else if(fileItem.getFieldName().equals("success")){
          successMethod = fileItem.getString();
        }
      }
      
      DeploymentBuilder deploymentBuilder = ActivitiUtil.getRepositoryService().createDeployment();
      String fileName = uploadItem.getName();
      if (fileName.endsWith(".bpmn20.xml")) {
        deploymentBuilder.addInputStream(fileName, uploadItem.getInputStream());
      } else if (fileName.endsWith(".bar") || fileName.endsWith(".zip")) {
        deploymentBuilder.addZipInputStream(new ZipInputStream(uploadItem.getInputStream()));
      } else {
        throw new ActivitiException("File must be of type .bpmn20.xml, .bar or .zip");
      }
      deploymentBuilder.name(fileName);
      deploymentBuilder.deploy();
      StringBuilder html = new StringBuilder();
      html.append("<html>\n");
      html.append("  <script type=\"text/javascript\">\n");
      html.append("    " + successMethod + "()\n");
      html.append("   </script>\n");
      html.append("</html>\n");
      getResponse().setEntity(html.toString(), MediaType.TEXT_HTML);
      
    } catch (Exception e) {
      throw new ActivitiException(e.getMessage(), e);
    }
  }
}
