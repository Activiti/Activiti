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

/**
 * Wraps an Activiti 5 attachment to an Activiti 6 {@link Attachment}.
 * 
 * @author Tijs Rademakers
 */
public class Activiti5AttachmentWrapper implements Attachment {

  private org.activiti5.engine.task.Attachment activit5Attachment;
  
  public Activiti5AttachmentWrapper(org.activiti5.engine.task.Attachment activit5Attachment) {
    this.activit5Attachment = activit5Attachment;
  }
  
  @Override
  public String getId() {
    return activit5Attachment.getId();
  }

  @Override
  public String getName() {
    return activit5Attachment.getName();
  }

  @Override
  public void setName(String name) {
    activit5Attachment.setName(name);
  }

  @Override
  public String getDescription() {
    return activit5Attachment.getDescription();
  }

  @Override
  public void setDescription(String description) {
    activit5Attachment.setDescription(description);
  }

  @Override
  public String getType() {
    return activit5Attachment.getType();
  }

  @Override
  public String getTaskId() {
    return activit5Attachment.getTaskId();
  }

  @Override
  public String getProcessInstanceId() {
    return activit5Attachment.getProcessInstanceId();
  }

  @Override
  public String getUrl() {
    return activit5Attachment.getUrl();
  }

  @Override
  public String getUserId() {
    return activit5Attachment.getUserId();
  }

  @Override
  public Date getTime() {
    return activit5Attachment.getTime();
  }

  @Override
  public void setTime(Date time) {
    activit5Attachment.setTime(time);
  }

  public org.activiti5.engine.task.Attachment getRawObject() {
    return activit5Attachment;
  }

}
