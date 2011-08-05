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

package org.activiti.engine.impl.mail;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.AttachmentEntity;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;


/**
 * @author Tom Baeyens
 */
public class MailScanCmd implements Command<Object> {
  
  private static Logger log = Logger.getLogger(MailScanCmd.class.getName());

  protected String userId;
  protected String imapUsername;
  protected String imapPassword;
  protected String imapHost;
  protected String imapProtocol;
  protected String toDoFolderName;
  protected String toDoInActivitiFolderName;
  
  public Object execute(CommandContext commandContext) {
    log.fine("scanning mail for user "+userId);

    Store store = null;
    Folder toDoFolder = null;
    Folder toDoInActivitiFolder = null;
    
    try {

      Session session = Session.getDefaultInstance(new Properties());
      store = session.getStore(imapProtocol);
      log.fine("connecting to "+imapHost+" over "+imapProtocol+" for user "+imapUsername);
      store.connect(imapHost, imapUsername, imapPassword);

      toDoFolder = store.getFolder(toDoFolderName);
      toDoFolder.open(Folder.READ_WRITE);
      toDoInActivitiFolder = store.getFolder(toDoInActivitiFolderName);
      toDoInActivitiFolder.open(Folder.READ_WRITE);
      
      Message[] messages = toDoFolder.getMessages();
      log.fine("getting messages from myToDoFolder");

      DbSqlSession dbSqlSession = commandContext.getDbSqlSession();

      for (Message message: messages) {
        log.fine("transforming mail into activiti task: "+message.getSubject());
        MailTransformer mailTransformer = new MailTransformer(message);

        createTask(commandContext, dbSqlSession, mailTransformer);
        
        Message[] messagesToCopy = new Message[]{message};
        toDoFolder.copyMessages(messagesToCopy, toDoInActivitiFolder);
        message.setFlag(Flags.Flag.DELETED, true);
      }

    } catch (RuntimeException e) {
      throw e;
      
    } catch (Exception e) {
      throw new ActivitiException("couldn't scan mail for user "+userId+": "+e.getMessage(), e);
      
    } finally {
      if (toDoInActivitiFolder!=null && toDoInActivitiFolder.isOpen()) {
        try {
          toDoInActivitiFolder.close(false);
        } catch (MessagingException e) {
          e.printStackTrace();
        }
      }
      if (toDoFolder!=null && toDoFolder.isOpen()) {
        try {
          toDoFolder.close(true); // true means that all messages that are flagged for deletion are permanently removed 
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (store!=null) {
        try {
          store.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }


  public void createTask(CommandContext commandContext, DbSqlSession dbSqlSession, MailTransformer mailTransformer) throws MessagingException {
    // distill the task description from the mail body content (without the html tags)
    String taskDescription = mailTransformer.getHtml();
    taskDescription = taskDescription.replaceAll("\\<.*?\\>", "");
    taskDescription = taskDescription.replaceAll("\\s", " ");
    taskDescription = taskDescription.trim();
    if (taskDescription.length()>120) {
      taskDescription = taskDescription.substring(0, 117)+"...";
    }

    // create and insert the task
    TaskEntity task = new TaskEntity();
    task.setAssignee(userId);
    task.setName(mailTransformer.getMessage().getSubject());
    task.setDescription(taskDescription);
    dbSqlSession.insert(task);
    String taskId = task.getId();
    
    // add identity links for all the recipients
    for (String recipientEmailAddress: mailTransformer.getRecipients()) {
      User recipient = new UserQueryImpl(commandContext)
        .userEmail(recipientEmailAddress)
        .singleResult();
      if (recipient!=null) {
        task.addUserIdentityLink(recipient.getId(), "Recipient");
      }
    }
    
    // attach the mail and other attachments to the task
    List<AttachmentEntity> attachments = mailTransformer.getAttachments();
    for (AttachmentEntity attachment: attachments) {
      // insert the bytes as content
      ByteArrayEntity content = attachment.getContent();
      dbSqlSession.insert(content);
      // insert the attachment
      attachment.setContentId(content.getId());
      attachment.setTaskId(taskId);
      dbSqlSession.insert(attachment);
    }
  }

  
  public String getUserId() {
    return userId;
  }

  
  public void setUserId(String userId) {
    this.userId = userId;
  }

  
  public String getImapUsername() {
    return imapUsername;
  }

  
  public void setImapUsername(String imapUsername) {
    this.imapUsername = imapUsername;
  }

  
  public String getImapPassword() {
    return imapPassword;
  }

  
  public void setImapPassword(String imapPassword) {
    this.imapPassword = imapPassword;
  }

  
  public String getImapHost() {
    return imapHost;
  }

  
  public void setImapHost(String imapHost) {
    this.imapHost = imapHost;
  }

  
  public String getImapProtocol() {
    return imapProtocol;
  }

  
  public void setImapProtocol(String imapProtocol) {
    this.imapProtocol = imapProtocol;
  }

  
  public String getToDoFolderName() {
    return toDoFolderName;
  }

  
  public void setToDoFolderName(String toDoFolderName) {
    this.toDoFolderName = toDoFolderName;
  }

  
  public String getToDoInActivitiFolderName() {
    return toDoInActivitiFolderName;
  }

  
  public void setToDoInActivitiFolderName(String toDoInActivitiFolderName) {
    this.toDoInActivitiFolderName = toDoInActivitiFolderName;
  }
}
