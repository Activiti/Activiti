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

package org.activiti.explorer.ui.custom;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.cache.UserCache;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.util.InputStreamStreamSource;

import com.vaadin.event.MouseEvents;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Component that renders link that shows the user's profile in a popup. Optionally, shows
 * the profile picture (if available).
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class UserProfileLink extends HorizontalLayout {

  private static final long serialVersionUID = 1L;
  
  protected IdentityService identityService;
  protected ViewManager viewManager;
  protected UserCache userCache;
  
  public UserProfileLink(IdentityService identityService, boolean renderPicture, final String userId) {
    this.identityService = identityService;
    this.viewManager = ExplorerApp.get().getViewManager();
    this.userCache = ExplorerApp.get().getUserCache();
    
    setSizeUndefined();
    setSpacing(true);
    addStyleName(ExplorerLayout.STYLE_PROFILE_LINK);
    
    initPicture(identityService, renderPicture, userId);
    initUserLink(userId);
  }

  protected void initPicture(IdentityService identityService, boolean renderPicture, final String userName) {
    if(renderPicture) {
      Picture picture = identityService.getUserPicture(userName);
      if(picture != null) {
        Resource imageResource = new StreamResource(new InputStreamStreamSource(picture.getInputStream()), 
          userName + picture.getMimeType(), ExplorerApp.get());
        
        Embedded image = new Embedded(null, imageResource);
        image.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
        image.setType(Embedded.TYPE_IMAGE);
        image.setHeight(30, Embedded.UNITS_PIXELS);
        image.setWidth(30, Embedded.UNITS_PIXELS);
        image.addListener(new MouseEvents.ClickListener() {
          private static final long serialVersionUID = 7341560240277898495L;
          public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
            viewManager.showProfilePopup(userName);
          }
        });
        
        addComponent(image);
        setComponentAlignment(image, Alignment.MIDDLE_LEFT);
      } else {
       // TODO: what when no image is available?
      }
    }
  }
  
  protected void initUserLink(final String userId) {
    User user = userCache.findUser(userId);
    Button userButton = new Button(user.getFirstName() + " " + user.getLastName());
    ClickListener buttonClickListener = new ClickListener() {
      public void buttonClick(ClickEvent event) {
        viewManager.showProfilePopup(userId);
      }
    };
    userButton.addStyleName(Reindeer.BUTTON_LINK);
    userButton.addListener(buttonClickListener);
    addComponent(userButton);
    setComponentAlignment(userButton, Alignment.MIDDLE_LEFT);
  }
  
}
