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

package org.activiti.rest.service.api.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author Tijs Rademakers
 * @author Frederik Heremans
 */
public class DeploymentCollectionResource extends SecuredResource {
  
  protected static final String DEPRECATED_API_DEPLOYMENT_SEGMENT = "deployment";
  
  private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();
  
  static {
    allowedSortProperties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
    allowedSortProperties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
    allowedSortProperties.put("deployTime", DeploymentQueryProperty.DEPLOY_TIME);
    allowedSortProperties.put("tenantId", DeploymentQueryProperty.DEPLOYMENT_TENANT_ID);
  }
  
  @Get("json")
  public DataResponse getDeployments() {
  	if(!authenticate()) { return null; }
  	
    DeploymentQuery deploymentQuery = ActivitiUtil.getRepositoryService().createDeploymentQuery();
    
    Form query = getQuery();
    Set<String> names = query.getNames();
    
    // Apply filters
    if(names.contains("name")) {
      deploymentQuery.deploymentName(getQueryParameter("name", query));
    }
    if(names.contains("nameLike")) {
      deploymentQuery.deploymentNameLike(getQueryParameter("nameLike", query));
    }
    if(names.contains("category")) {
      deploymentQuery.deploymentCategory(getQueryParameter("category", query));
    }
    if(names.contains("categoryNotEquals")) {
      deploymentQuery.deploymentCategoryNotEquals(getQueryParameter("categoryNotEquals", query));
    }
    if(names.contains("tenantId")) {
      deploymentQuery.deploymentTenantId(getQueryParameter("tenantId", query));
    }
    if(names.contains("tenantIdLike")) {
      deploymentQuery.deploymentTenantIdLike(getQueryParameter("tenantIdLike", query));
    }
    if(names.contains("withoutTenantId")) {
    	Boolean withoutTenantId = getQueryParameterAsBoolean("withoutTenantId", query);
    	if(Boolean.TRUE == withoutTenantId) {
    		deploymentQuery.deploymentWithoutTenantId();
    	}
    }

    DataResponse response = new DeploymentsPaginateList(this).paginateList(getQuery(), 
        deploymentQuery, "id", allowedSortProperties);
    return response;
  }
  
  @Post
  public DeploymentResponse uploadDeployment(Representation entity) {
  	if(!authenticate()) { return null; }
  	
    try {

      if(entity == null || entity.getMediaType() == null || !MediaType.MULTIPART_FORM_DATA.isCompatible(entity.getMediaType())) {
        throw new ActivitiIllegalArgumentException("The request should be of type" + MediaType.MULTIPART_FORM_DATA  +".");
      }
      
      RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
      List<FileItem> items = upload.parseRepresentation(entity);
      
      String tenantId = null;
      
      FileItem uploadItem = null;
      for (FileItem fileItem : items) {
      	if(fileItem.isFormField()) {
      		if("tenantId".equals(fileItem.getFieldName())) {
      			tenantId = fileItem.getString("UTF-8");
      		}
      	} else if(fileItem.getName() != null) {
          uploadItem = fileItem;
        }
      }
      
      if(uploadItem == null) {
        throw new ActivitiIllegalArgumentException("No file content was found in request body.");
      }
      
      DeploymentBuilder deploymentBuilder = ActivitiUtil.getRepositoryService().createDeployment();
      String fileName = uploadItem.getName();
      if (fileName.endsWith(".bpmn20.xml") || fileName.endsWith(".bpmn")) {
        deploymentBuilder.addInputStream(fileName, uploadItem.getInputStream());
      } else if (fileName.toLowerCase().endsWith(".bar") || fileName.toLowerCase().endsWith(".zip")) {
        deploymentBuilder.addZipInputStream(new ZipInputStream(uploadItem.getInputStream()));
      } else {
        throw new ActivitiIllegalArgumentException("File must be of type .bpmn20.xml, .bpmn, .bar or .zip");
      }
      deploymentBuilder.name(fileName);
      
      if(tenantId != null) {
      	deploymentBuilder.tenantId(tenantId);
      }
      
      Deployment deployment = deploymentBuilder.deploy();
      
      setStatus(Status.SUCCESS_CREATED);
      
      return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
              .createDeploymentResponse(this, deployment);
      
    } catch (Exception e) {
      if(e instanceof ActivitiException) {
        throw (ActivitiException) e;
      }
      throw new ActivitiException(e.getMessage(), e);
    }
  }
}
