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

package org.activiti.engine.impl.cmd;

import java.io.InputStream;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.AttachmentEntity;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Event;


/**
 * @author Tom Baeyens
 */
public class CreateAttachmentCmd implements Command<Attachment> {

  protected String attachmentType;
  protected String taskId;
  protected String processInstanceId;
  protected String attachmentName;
  protected String attachmentDescription;
  protected InputStream content;
  protected String url;
  
  public CreateAttachmentCmd(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content, String url) {
    this.attachmentType = attachmentType;
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.attachmentName = attachmentName;
    this.attachmentDescription = attachmentDescription;
    this.content = content;
    this.url = url;
  }

  public Attachment execute(CommandContext commandContext) {
    AttachmentEntity attachment = new AttachmentEntity();
    attachment.setName(attachmentName);
    attachment.setDescription(attachmentDescription);
    attachment.setType(attachmentType);
    attachment.setTaskId(taskId);
    attachment.setProcessInstanceId(processInstanceId);
    attachment.setUrl(url);
    
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.insert(attachment);
    
    if (content!=null) {
      byte[] bytes = IoUtil.readInputStream(content, attachmentName);
      ByteArrayEntity byteArray = new ByteArrayEntity(bytes);
      dbSqlSession.insert(byteArray);
      attachment.setContentId(byteArray.getId());
    }
    
    String userId = Authentication.getAuthenticatedUserId();
    CommentEntity comment = new CommentEntity();
    comment.setUserId(userId);
    comment.setType(CommentEntity.TYPE_EVENT);
    comment.setTime(ClockUtil.getCurrentTime());
    comment.setTaskId(taskId);
    comment.setProcessInstanceId(processInstanceId);
    comment.setAction(Event.ACTION_ADD_ATTACHMENT);
    comment.setMessage(attachmentName);
    commandContext
      .getCommentManager()
      .insert(comment);
    
    return attachment;
  }

}
