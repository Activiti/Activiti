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

package org.activiti.rest.service.api.engine;

import java.util.Date;

import org.activiti.rest.common.util.DateToStringSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * @author Frederik Heremans
 */
public class AttachmentResponse {

  private String id;
  private String url;
  private String name;
  private String userId;
  private String description;
  private String type;
  private String taskUrl;
  private String processInstanceUrl;
  private String externalUrl;
  private String contentUrl;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  private Date time;
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getTaskUrl() {
    return taskUrl;
  }
  
  public void setTaskUrl(String taskUrl) {
    this.taskUrl = taskUrl;
  }
  
  public String getProcessInstanceUrl() {
    return processInstanceUrl;
  }
  
  public void setProcessInstanceUrl(String processInstanceUrl) {
    this.processInstanceUrl = processInstanceUrl;
  }
  
  public String getExternalUrl() {
    return externalUrl;
  }
  
  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }
  
  public String getContentUrl() {
    return contentUrl;
  }
  
  public void setContentUrl(String contentUrl) {
    this.contentUrl = contentUrl;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }
}
