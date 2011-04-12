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
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
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
public class TaskEventPopupWindow extends Window {
  
  private static final long serialVersionUID = 1L;
  
  protected IdentityService identityService;
  protected I18nManager i18nManager;
  
  protected org.activiti.engine.task.Event taskEvent;
  protected User user;

  public TaskEventPopupWindow(org.activiti.engine.task.Event taskEvent) {
    super();
    this.taskEvent = taskEvent;
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.user = identityService.createUserQuery().userId(taskEvent.getUserId()).singleResult();
    
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
      }, user.getId(), ExplorerApp.get());
      
      Embedded author = new Embedded("", imageresource);
      author.setType(Embedded.TYPE_IMAGE);
      author.setHeight("200px");
      author.setWidth("200px");
      author.addStyleName(ExplorerLayout.STYLE_PROFILE_PICTURE);
      
      layout.addComponent(author);
    }
    
    VerticalLayout eventLayout = new VerticalLayout();
    eventLayout.setSpacing(true);
    eventLayout.setWidth("70%");
    layout.addComponent(eventLayout);
    layout.setExpandRatio(eventLayout, 1.0f);
    
    Label header = new Label(i18nManager.getMessage(
        Messages.TASK_COMMENT_POPUP_HEADER,
        new PrettyTime().format(taskEvent.getTime()),
        user.getFirstName() + " " + user.getLastName()));
    header.addStyleName(ExplorerLayout.STYLE_TASK_EVENT_AUTHOR);
    eventLayout.addComponent(header);
    
    Label text = new Label(taskEvent.getMessage());
    eventLayout.addComponent(text);
  }

}
