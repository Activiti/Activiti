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
import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Joram Barrez
 */
public class UserDetailsComponent extends HorizontalLayout implements LayoutClickListener {
    
    private static final long serialVersionUID = 1L;
    
    protected IdentityService identityService;
    protected ViewManager viewManager;
    
    protected String role;
    protected User user;

    public UserDetailsComponent(String userId, String role) {
      this.role = role;
      identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
      viewManager = ExplorerApp.get().getViewManager();
      
      if (userId != null) {
        user = identityService.createUserQuery().userId(userId).singleResult();
      }
      
      // init UI
      addUserPicture();
      addUserDetails();
      
      // Init click listener (show profile popup)
      addListener(this);
    }

    protected void addUserPicture() {
      Embedded picture = null;
      if (user != null) {
        StreamResource imageresource = new StreamResource(new StreamSource() {        
          public InputStream getStream() {
            return identityService.getUserPicture(user.getId()).getInputStream();
          }
        }, user.getId(), ExplorerApp.get());
        picture = new Embedded(null, imageresource);
      } else {
        picture = new Embedded(null, Images.USER_48);
      }
      
      picture.setType(Embedded.TYPE_IMAGE);
      picture.addStyleName(ExplorerLayout.STYLE_TASK_COMMENT_PICTURE);
      picture.setHeight("48px");
      picture.setWidth("48px");
      addComponent(picture);
    }
    
    protected void addUserDetails() {
      VerticalLayout layout = new VerticalLayout();
      addComponent(layout);
      setExpandRatio(layout, 1.0f);
      
      // Name
      Label nameLabel = null;
      if (user != null) {
        nameLabel = new Label(user.getFirstName() + " " + user.getLastName());
        layout.addComponent(nameLabel);
      }
      
      // Role
      Label roleLabel = new Label(role);
      layout.addComponent(roleLabel);
    }
    
    public void layoutClick(LayoutClickEvent event) {
      viewManager.showProfilePopup(user.getId());
    }
    
  }