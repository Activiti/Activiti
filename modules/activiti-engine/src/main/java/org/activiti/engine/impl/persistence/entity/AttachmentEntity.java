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

package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.task.Attachment;


/**
 * @author Tom Baeyens
 */
public class AttachmentEntity implements Attachment, PersistentObject, HasRevision, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String id;
  protected int revision;
  protected String name;
  protected String description;
  protected String type;
  protected String taskId;
  protected String processInstanceId;
  protected String url;
  protected String contentId;
  protected ByteArrayEntity content;
  protected String userId;
  protected Date time;

  public AttachmentEntity() {
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", name);
    persistentState.put("description", description);
    return persistentState;
  }

  public int getRevisionNext() {
    return revision+1;
  }
  
  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public int getRevision() {
    return revision;
  }

  
  public void setRevision(int revision) {
    this.revision = revision;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
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

  
  public String getTaskId() {
    return taskId;
  }

  
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  
  public String getUrl() {
    return url;
  }

  
  public void setUrl(String url) {
    this.url = url;
  }

  
  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }
  
  public ByteArrayEntity getContent() {
    return content;
  }

  public void setContent(ByteArrayEntity content) {
    this.content = content;
  }
  
  public void setUserId(String userId) {
	this.userId = userId;
  }
  
  public String getUserId() {
	return userId;
  }
  
  public Date getTime() {
    return time;
  }
  
  public void setTime(Date time) {
    this.time = time;
  }
  
}
