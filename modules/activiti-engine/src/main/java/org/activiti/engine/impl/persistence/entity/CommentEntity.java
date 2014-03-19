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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;


/**
 * @author Tom Baeyens
 */
public class CommentEntity implements Comment, Event, PersistentObject, Serializable {
  
  private static final long serialVersionUID = 1L;
  
  public static final String TYPE_EVENT = "event";
  public static final String TYPE_COMMENT = "comment";
  
  protected String id;
  
  // If comments would be removeable, revision needs to be added!
  
  protected String type;
  protected String userId;
  protected Date time;
  protected String taskId;
  protected String processInstanceId;
  protected String action;
  protected String message;
  protected String fullMessage;
  
  public Object getPersistentState() {
    return CommentEntity.class;
  }

  public byte[] getFullMessageBytes() {
    return (fullMessage!=null ? fullMessage.getBytes() : null);
  }

  public void setFullMessageBytes(byte[] fullMessageBytes) {
    fullMessage = (fullMessageBytes!=null ? new String(fullMessageBytes) : null );
  }
  
  public static String MESSAGE_PARTS_MARKER = "_|_";
  public static Pattern MESSAGE_PARTS_MARKER_REGEX = Pattern.compile("_\\|_");
  
  public void setMessage(String[] messageParts) {
    StringBuilder stringBuilder = new StringBuilder();
    for (String part: messageParts) {
      if (part!=null) {
        stringBuilder.append(part.replace(MESSAGE_PARTS_MARKER, " | "));
        stringBuilder.append(MESSAGE_PARTS_MARKER);
      } else {
        stringBuilder.append("null");
        stringBuilder.append(MESSAGE_PARTS_MARKER);
      }
    }
    for (int i=0; i<MESSAGE_PARTS_MARKER.length(); i++) {
      stringBuilder.deleteCharAt(stringBuilder.length()-1);
    }
    message = stringBuilder.toString();
  }
  
  public List<String> getMessageParts() {
    if (message==null) {
      return null;
    }
    List<String> messageParts = new ArrayList<String>();
    
    String[] parts = MESSAGE_PARTS_MARKER_REGEX.split(message);
    for(String part : parts) {
      if ("null".equals(part)) {
        messageParts.add(null);
      } else {
        messageParts.add(part);
      }
    }
    return messageParts;
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getUserId() {
    return userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public String getTaskId() {
    return taskId;
  }
  
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
  
  public String getMessage() {
    return message;
  }
  
  public void setMessage(String message) {
    this.message = message;
  }
  
  public Date getTime() {
    return time;
  }
  
  public void setTime(Date time) {
    this.time = time;
  }
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }

  public String getFullMessage() {
    return fullMessage;
  }

  public void setFullMessage(String fullMessage) {
    this.fullMessage = fullMessage;
  }

  public String getAction() {
    return action;
  }
  
  public void setAction(String action) {
    this.action = action;
  }
}
