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

package org.activiti.explorer.ui.task;

import org.activiti.engine.identity.User;
import org.activiti.engine.task.Event;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.cache.UserCache;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Label;


/**
 * Helper class that resolves a task {@link Event} to a Label
 * that contains all the information in the event.
 * 
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class TaskEventTextResolver {
  
  protected I18nManager i18nManager;
  protected UserCache userCache;
  
  public TaskEventTextResolver() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.userCache = ExplorerApp.get().getUserCache();
  }
  
  public Label resolveText(Event event) {
    User user = userCache.findUser(event.getUserId());
    String eventAuthor = "<span class='" + ExplorerLayout.STYLE_TASK_EVENT_AUTHOR + "'>" 
          + user.getFirstName() + " " + user.getLastName() + "</span> ";
    
    String text = null;
    if (Event.ACTION_ADD_USER_LINK.equals(event.getAction())) {
      User involvedUser = userCache.findUser(event.getMessageParts().get(0));
      text = i18nManager.getMessage(Messages.EVENT_ADD_USER_LINK, 
              eventAuthor, 
              involvedUser.getFirstName() + " " + involvedUser.getLastName(),
              event.getMessageParts().get(1)); // second msg part = role
    } else if (Event.ACTION_DELETE_USER_LINK.equals(event.getAction())) {
      User involvedUser = userCache.findUser(event.getMessageParts().get(0));
      text = i18nManager.getMessage(Messages.EVENT_DELETE_USER_LINK, 
              eventAuthor, 
              involvedUser.getFirstName() + " " + involvedUser.getLastName(),
              event.getMessageParts().get(1));
    } else if (Event.ACTION_ADD_GROUP_LINK.equals(event.getAction())) {
      text = i18nManager.getMessage(Messages.EVENT_ADD_GROUP_LINK, 
              eventAuthor, 
              event.getMessageParts().get(0),
              event.getMessageParts().get(1)); // second msg part = role
    } else if (Event.ACTION_DELETE_GROUP_LINK.equals(event.getAction())) {
        text = i18nManager.getMessage(Messages.EVENT_DELETE_GROUP_LINK, 
                eventAuthor, 
                event.getMessageParts().get(0),
                event.getMessageParts().get(1)); // second msg part = role
    } else if (Event.ACTION_ADD_ATTACHMENT.equals(event.getAction())) {
      text = i18nManager.getMessage(Messages.EVENT_ADD_ATTACHMENT, eventAuthor, event.getMessage());
    } else if (Event.ACTION_DELETE_ATTACHMENT.equals(event.getAction())) {
      text = i18nManager.getMessage(Messages.EVENT_DELETE_ATTACHMENT, eventAuthor, event.getMessage());
    } else if (Event.ACTION_ADD_COMMENT.equals(event.getAction())) {
      text = i18nManager.getMessage(Messages.EVENT_COMMENT, eventAuthor, event.getMessage());
    } else { // default: just show the message
      text += i18nManager.getMessage(Messages.EVENT_DEFAULT, eventAuthor, event.getMessage());
    }
    return new Label(text, Label.CONTENT_XHTML);
  }

}
