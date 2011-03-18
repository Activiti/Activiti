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

package org.activiti.explorer.ui.profile;

import java.io.InputStream;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.explorer.Constants;
import org.activiti.explorer.Images;
import org.activiti.explorer.ui.ViewManager;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ProfilePage extends Panel {
  
  private static final long serialVersionUID = -4274649964206760400L;
  
  // service
  protected IdentityService identityService;
  
  // user information
  protected String userId;
  protected User user;
  protected Picture picture;
  protected String birthDate;
  protected String jobTitle;
  protected String location;
  protected String phone;
  protected String twitterName;
  protected String skypeId;
  
  // ui
  protected ViewManager viewManager;
  protected HorizontalLayout profilePanelLayout;
  protected VerticalLayout infoPanelLayout;
  
  public ProfilePage(ViewManager viewManager, String userId) {
    this.viewManager = viewManager;
    this.userId = userId;
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    
    loadProfileData();
    initUi();
  }
  
  protected void loadProfileData() {
    this.user = identityService.createUserQuery().userId(userId).singleResult();
    this.picture = identityService.getUserPicture(user.getId());
    this.birthDate = identityService.getUserInfo(user.getId(), "birthDate");
    this.jobTitle = identityService.getUserInfo(user.getId(), "jobTitle");
    this.location = identityService.getUserInfo(user.getId(), "location");
    this.phone = identityService.getUserInfo(user.getId(), "phone");
    this.twitterName = identityService.getUserInfo(user.getId(), "twitterName");
    this.skypeId = identityService.getUserInfo(user.getId(), "skype");
  }

  protected void initUi() {
    addStyleName(Reindeer.PANEL_LIGHT);
    setSizeFull();
    
    // Profile page is a horizontal layout: left we have a panel with the picture, 
    // and one the right there is another panel the about, contact, etc information
    this.profilePanelLayout = new HorizontalLayout();
    profilePanelLayout.setSizeFull();
    setContent(profilePanelLayout);
    
    // init both panels
    initImagePanel();
    initInformationPanel();
  }
  
  protected void initImagePanel() {
    // Panel
    Panel imagePanel = new Panel();
    imagePanel.addStyleName(Reindeer.PANEL_LIGHT);
    
    // Image
    StreamResource imageresource = new StreamResource(new StreamSource() {
      private static final long serialVersionUID = -8875067466181823014L;
      public InputStream getStream() {
        return picture.getInputStream();
      }
    }, user.getId(), viewManager.getApplication());
    
    Embedded image = new Embedded("", imageresource);
    image.setType(Embedded.TYPE_IMAGE);
    image.setHeight("200px");
    image.setWidth("200px");
    image.addStyleName(Constants.STYLE_PROFILE_PICTURE);
    
    imagePanel.addComponent(image);
    imagePanel.setHeight("100%");
    imagePanel.getContent().setHeight("100%");
    imagePanel.setWidth(image.getWidth() + 50, image.getWidthUnits()); // adding 40 to have more whitespace
    
    profilePanelLayout.addComponent(imagePanel);
  }
  
  protected void initInformationPanel() {
    Panel infoPanel = new Panel();
    infoPanel.addStyleName(Reindeer.PANEL_LIGHT);
    infoPanel.setSizeFull();
    
    profilePanelLayout.addComponent(infoPanel);
    profilePanelLayout.setExpandRatio(infoPanel, 1.0f); // info panel should take all the remaining width available
    
    // All the information sections are put under each other in a vertical layout
    this.infoPanelLayout = new VerticalLayout();
    infoPanel.setContent(infoPanelLayout);
    
    initAboutSection();
    initContactSection();
    initAccountsSection();
  }

  protected void initAboutSection() {
    addProfileHeader(infoPanelLayout, "About");
    GridLayout aboutLayout = createInfoSectionLayout(2, 4); 

    addProfileEntry(aboutLayout, "Name: ", user.getFirstName() + " " + user.getLastName());
    
    if (jobTitle != null) {
      addProfileEntry(aboutLayout, "Job title: ", jobTitle);
    }
    
    if (birthDate != null) {
      addProfileEntry(aboutLayout, "Birth date: ", birthDate);
    }
    
    if (location != null) {
      addProfileEntry(aboutLayout, "Location: ", location);
    }
  }
  
  protected void initContactSection() {
    addProfileHeader(infoPanelLayout, "Contact");
    GridLayout contactLayout = createInfoSectionLayout(2, 4); 
    
    if (user.getEmail() != null) {
      addProfileEntry(contactLayout, "Email: ", user.getEmail());
    }
    
    if (phone != null) {
      addProfileEntry(contactLayout, "Phone: ", phone);
    }
    
    if (twitterName != null) {
      addProfileEntry(contactLayout, "twitter", twitterName);
    }

    if (skypeId != null) {
      // The skype entry shows the name + skype icon, laid out in a small grid
      GridLayout skypeLayout = new GridLayout(3,1);
      skypeLayout.setSizeUndefined();
      
      Label skypeIdLabel = new Label(skypeId);
      skypeIdLabel.setSizeUndefined();
      skypeLayout.addComponent(skypeIdLabel);
      
      Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
      emptySpace.setSizeUndefined();
      skypeLayout.addComponent(emptySpace);
      
      Embedded skypeImage = new Embedded(null, viewManager.getClassResource("images/skype.png"));
      skypeImage.setSizeUndefined();
      skypeLayout.addComponent(skypeImage);
      
      addProfileEntry(contactLayout, "Skype: ", skypeLayout);
    }
  }
  
  protected void  initAccountsSection() {
    addProfileHeader(infoPanelLayout, "Accounts");
    GridLayout accountLayout = createInfoSectionLayout(3, 2); 

    // Google
    Embedded googleImage = new Embedded(null, Images.GOOGLE_IMAGE);
    googleImage.setSizeUndefined();
    accountLayout.addComponent(googleImage);
    addProfileEntry(accountLayout, "Google: ", "mr_kermit_frog@gmail.com");
    
    // Alfresco
    Embedded alfrescoImage = new Embedded(null, Images.ALFRESCO_IMAGE);
    alfrescoImage.setSizeUndefined();
    accountLayout.addComponent(alfrescoImage);
    addProfileEntry(accountLayout, "Alfresco: ", "kermit_alfresco");
  }
  
  protected void addProfileHeader(VerticalLayout infoLayout, String headerName) {
    Label aboutLabel = new Label(headerName);
    aboutLabel.setWidth("90%");
    aboutLabel.addStyleName(Constants.STYLE_PROFILE_HEADER);
    infoLayout.addComponent(aboutLabel);
  }
  
  protected GridLayout createInfoSectionLayout(int columns, int rows) {
    GridLayout layout = new GridLayout(columns, rows);
    layout.setSpacing(true);
    layout.addStyleName(Constants.STYLE_PROFILE_LAYOUT);
    infoPanelLayout.addComponent(layout);
    return layout;
  }
  
  protected void addProfileEntry(GridLayout layout, String name, String value) {
    addProfileEntry(layout, name, new Label(value));
  }
  
  protected void addProfileEntry(GridLayout layout, String name, Component value) {
    addProfileEntry(layout, new Label(name), value);
  }
  
  protected void addProfileEntry(GridLayout layout, Component name, Component value) {
    name.addStyleName(Constants.STYLE_PROFILE_FIELD);
    name.setSizeUndefined();
    layout.addComponent(name);
    
    value.setSizeUndefined();
    layout.addComponent(value);
  }

}
