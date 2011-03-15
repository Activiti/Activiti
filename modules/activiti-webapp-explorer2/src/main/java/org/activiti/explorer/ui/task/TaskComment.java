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
import org.activiti.engine.form.Comment;
import org.activiti.engine.identity.User;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ui.ViewManager;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * @author Joram Barrez
 */
public class TaskComment extends CustomComponent {
  
  private static final long serialVersionUID = -1010016850055114137L;
  
  protected ViewManager viewManager;
  protected Comment comment;
  protected User user;
  protected IdentityService identityService;

  public TaskComment(ViewManager viewManager, Comment comment) {
    this.viewManager = viewManager;
    this.comment = comment;
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.user = identityService.createUserQuery().userId(comment.getUserId()).singleResult();
    
    initUi();
  }
  
  protected void initUi() {
    addStyleName(Constants.STYLE_TASK_COMMENT);
    
    HorizontalLayout layout = new HorizontalLayout();
    setCompositionRoot(layout);
    layout.setSpacing(true);
    layout.setWidth("100%");
    
    addPicture(layout);
    addCommentText(layout);
  }
  
  protected void addPicture(HorizontalLayout layout) {
    StreamResource imageresource = new StreamResource(new StreamSource() {
      private static final long serialVersionUID = -8875067466181823014L;
      public InputStream getStream() {
        return identityService.getUserPicture(comment.getUserId()).getInputStream();
      }
    }, user.getId(), viewManager.getApplication());
    
    Embedded picture = new Embedded("", imageresource);
    picture.setType(Embedded.TYPE_IMAGE);
    picture.setHeight("48px");
    picture.setWidth("48px");
    
    layout.addComponent(picture);
    layout.setComponentAlignment(picture, Alignment.TOP_LEFT);
  }
  
  protected void addCommentText(HorizontalLayout layout) {
    VerticalLayout commentLayout = new VerticalLayout();
    layout.addComponent(commentLayout);
    layout.setExpandRatio(commentLayout, 1.0f); // comment text takes all available space next to picture
    
    // Name
    Label name = new Label(user.getFirstName() + " " + user.getLastName());
    commentLayout.addComponent(name);
    
    // Actual text
    Label text = new Label(comment.getMessage());
    commentLayout.addComponent(text);
  }

}
