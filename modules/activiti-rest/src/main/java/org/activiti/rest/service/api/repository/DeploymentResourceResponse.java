package org.activiti.rest.service.api.repository;

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


/**
 * @author Frederik Heremans
 */
public class DeploymentResourceResponse {
  private String id;
  private String url;
  private String contentUrl;
  private String mediaType;
  private DeploymentResourceType type;
  
  public DeploymentResourceResponse(String resourceId, String url, String contentUrl, 
          String mediaType, DeploymentResourceType type) {
    setId(resourceId);
    setUrl(url);
    setContentUrl(contentUrl);
    setMediaType(mediaType);
    
    this.type = type;
    if(type == null) {
      this.type = DeploymentResourceType.RESOURCE;
    }
  }
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getUrl() {
    return url;
  }
  public void setContentUrl(String contentUrl) {
    this.contentUrl = contentUrl;
  }
  public String getContentUrl() {
    return contentUrl;
  }
  public void setMediaType(String mimeType) {
    this.mediaType = mimeType;
  }
  public String getMediaType() {
    return mediaType;
  } 
  public String getType() {
    switch (type) {
    case PROCESS_DEFINITION:
      return "processDefinition";
    case PROCESS_IMAGE:
      return "processImage";
    default:
      return "resource";
    }
  }
  
  /**
   * Possible types of resources in a deployment.
   * TODO: move to activiti API
   * 
   * @author Frederik Heremans
   */
  public enum DeploymentResourceType {
    /**
     * A simple resource in a deployment.
     */
    RESOURCE,
    /**
     * A resource that contains a process-definition.
     */
    PROCESS_DEFINITION,
    /**
     * A resource that is a process-definition image.
     */
    PROCESS_IMAGE;
  }
}
