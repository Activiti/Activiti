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

package org.activiti.compatibility.wrapper;

import java.util.Date;

import org.activiti.engine.task.Attachment;
import org.activiti5.engine.impl.persistence.entity.AttachmentEntity;

/**
 * Wraps an Activiti 5 attachment to an Activiti 6 {@link Attachment}.
 * 
 * @author Tijs Rademakers
 */
public class Activiti5AttachmentWrapper implements Attachment {

  private org.activiti5.engine.task.Attachment activiti5Attachment;
  
  public Activiti5AttachmentWrapper(org.activiti5.engine.task.Attachment activit5Attachment) {
    this.activiti5Attachment = activit5Attachment;
  }
  
  @Override
  public String getId() {
    return activiti5Attachment.getId();
  }

  @Override
  public String getName() {
    return activiti5Attachment.getName();
  }

  @Override
  public void setName(String name) {
    activiti5Attachment.setName(name);
  }

  @Override
  public String getDescription() {
    return activiti5Attachment.getDescription();
  }

  @Override
  public void setDescription(String description) {
    activiti5Attachment.setDescription(description);
  }

  @Override
  public String getType() {
    return activiti5Attachment.getType();
  }

  @Override
  public String getTaskId() {
    return activiti5Attachment.getTaskId();
  }

  @Override
  public String getProcessInstanceId() {
    return activiti5Attachment.getProcessInstanceId();
  }

  @Override
  public String getUrl() {
    return activiti5Attachment.getUrl();
  }

  @Override
  public String getUserId() {
    return activiti5Attachment.getUserId();
  }

  @Override
  public Date getTime() {
    return activiti5Attachment.getTime();
  }

  @Override
  public void setTime(Date time) {
    activiti5Attachment.setTime(time);
  }
  
  @Override
  public String getContentId() {
    return ((AttachmentEntity) activiti5Attachment).getContentId();
  }

  public org.activiti5.engine.task.Attachment getRawObject() {
    return activiti5Attachment;
  }

}
