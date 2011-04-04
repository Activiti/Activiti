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
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.profile.ProfilePopupWindow;
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
 */
public class UserProfileLink extends HorizontalLayout {

  private static final long serialVersionUID = -8732380136836785044L;
  
  protected boolean renderPicture;
  
  protected IdentityService identityService;
  
  public UserProfileLink(IdentityService identityService, boolean renderPicture, final String userName) {
    this.renderPicture = renderPicture;
    this.identityService = identityService;
    
    Button owner = new Button(userName);
    
    ClickListener buttonClickListener = new ClickListener() {
      private static final long serialVersionUID = 4629275955614367780L;

      public void buttonClick(ClickEvent event) {
        ExplorerApplication.getCurrent().showPopupWindow(new ProfilePopupWindow(userName));
      }
    };
    owner.addStyleName(Reindeer.BUTTON_LINK);
    owner.addListener(buttonClickListener);
    
    if(renderPicture) {
      // Also add the user's picture
      Picture picture = identityService.getUserPicture(userName);
      if(picture != null) {
        Resource imageResource = new StreamResource(new InputStreamStreamSource(picture.getInputStream()), 
          userName + picture.getMimeType(), ExplorerApplication.getCurrent());
        
        Embedded image = new Embedded("", imageResource);
        image.setType(Embedded.TYPE_IMAGE);
        image.setHeight(30, Embedded.UNITS_PIXELS);
        image.setWidth(30, Embedded.UNITS_PIXELS);
        image.addStyleName(ExplorerLayout.STYLE_PROFILE_PICTURE);
        image.addListener(new MouseEvents.ClickListener() {
          private static final long serialVersionUID = 7341560240277898495L;
          public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
            ExplorerApplication.getCurrent().showPopupWindow(new ProfilePopupWindow(userName));
          }
        });
        
        addComponent(image);
        setComponentAlignment(image, Alignment.MIDDLE_LEFT);
      } else {
       // TODO: what when no image is available?
      }
    }
    addComponent(owner);
    setComponentAlignment(owner, Alignment.MIDDLE_LEFT);
    
    setSizeUndefined();
    setSpacing(true);
  }
}
