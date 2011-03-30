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

import java.io.InputStream;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.Comment;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.ExplorerLayout;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class TaskCommentPopupWindow extends Window {
  
  private static final long serialVersionUID = -5764168454419580506L;
  
  protected Comment comment;
  protected IdentityService identityService;
  protected User user;

  public TaskCommentPopupWindow(Comment comment) {
    super();
    this.comment = comment;
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.user = identityService.createUserQuery().userId(comment.getUserId()).singleResult();
    
    initUi();
  }
  
  protected void initUi() {
    // general
    addStyleName(Reindeer.WINDOW_LIGHT);
    setModal(true);
    setHeight("50%");
    setWidth("40%");
    center();
    
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidth("100%");
    layout.setSpacing(true);
    addComponent(layout);
    
    // picture
    final Picture picture = identityService.getUserPicture(user.getId());
    if (picture != null) {
      StreamResource imageresource = new StreamResource(new StreamSource() {
        private static final long serialVersionUID = -8875067466181823014L;
        public InputStream getStream() {
          return picture.getInputStream();
        }
      }, user.getId(), ExplorerApplication.getCurrent());
      
      Embedded commentAuthor = new Embedded("", imageresource);
      commentAuthor.setType(Embedded.TYPE_IMAGE);
      commentAuthor.setHeight("200px");
      commentAuthor.setWidth("200px");
      commentAuthor.addStyleName(ExplorerLayout.STYLE_PROFILE_PICTURE);
      
      layout.addComponent(commentAuthor);
    }
    
    // comment
    VerticalLayout commentLayout = new VerticalLayout();
    commentLayout.setSpacing(true);
    commentLayout.setWidth("70%");
    layout.addComponent(commentLayout);
    layout.setExpandRatio(commentLayout, 1.0f);
    
    Label header = new Label(ExplorerApplication.getCurrent().getMessage(
        Messages.TASK_COMMENT_POPUP_HEADER,
        new PrettyTime().format(comment.getTime()),
        user.getFirstName() + " " + user.getLastName()));
    header.addStyleName(ExplorerLayout.STYLE_TASK_COMMENT_AUTHOR);
    commentLayout.addComponent(header);
    
    Label commentText = new Label(comment.getMessage());
    commentLayout.addComponent(commentText);
  }

}
