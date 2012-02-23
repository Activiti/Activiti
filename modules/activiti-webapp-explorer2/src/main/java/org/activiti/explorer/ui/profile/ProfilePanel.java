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
import java.text.SimpleDateFormat;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.InMemoryUploadReceiver;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ProfilePanel extends Panel {
  
  private static final long serialVersionUID = -4274649964206760400L;
  
  // services
  protected IdentityService identityService;
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  
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
  protected boolean isCurrentLoggedInUser;
  protected boolean editable = false;
  protected HorizontalLayout profilePanelLayout;
  protected VerticalLayout imageLayout;
  protected VerticalLayout infoPanelLayout;
  protected TextField firstNameField;
  protected TextField lastNameField;
  protected PasswordField passwordField;
  protected TextField jobTitleField;
  protected DateField birthDateField;
  protected TextField locationField;
  protected TextField emailField;
  protected TextField phoneField;
  protected TextField twitterField;
  protected TextField skypeField;
  
  public ProfilePanel(String userId) {
    this.userId = userId;
    this.isCurrentLoggedInUser = userId.equals(ExplorerApp.get().getLoggedInUser().getId());
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    
    loadProfileData();
    initUi();
  }
  
  protected void loadProfileData() {
    this.user = identityService.createUserQuery().userId(userId).singleResult();
    this.picture = identityService.getUserPicture(user.getId());
    this.birthDate = identityService.getUserInfo(user.getId(), Constants.USER_INFO_BIRTH_DATE);
    this.jobTitle = identityService.getUserInfo(user.getId(), Constants.USER_INFO_JOB_TITLE);
    this.location = identityService.getUserInfo(user.getId(), Constants.USER_INFO_LOCATION);
    this.phone = identityService.getUserInfo(user.getId(), Constants.USER_INFO_PHONE);
    this.twitterName = identityService.getUserInfo(user.getId(), Constants.USER_INFO_TWITTER);
    this.skypeId = identityService.getUserInfo(user.getId(), Constants.USER_INFO_SKYPE);
  }

  protected void initUi() {
    removeAllComponents();
    addStyleName(Reindeer.PANEL_LIGHT);
    addStyleName(ExplorerLayout.STYLE_PROFILE_LAYOUT);
    setSizeFull();
    
    // Profile page is a horizontal layout: left we have a panel with the picture, 
    // and one the right there is another panel the about, contact, etc information
    this.profilePanelLayout = new HorizontalLayout();
    profilePanelLayout.setSizeFull();
    setContent(profilePanelLayout);
    
    // init both panels
    initImagePanel();
    
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setWidth(50, UNITS_PIXELS);
    profilePanelLayout.addComponent(emptySpace);
    
    initInformationPanel();
  }
  
  protected void initImagePanel() {
    imageLayout = new VerticalLayout();
    imageLayout.setSpacing(true);
    imageLayout.setHeight("100%");
    profilePanelLayout.addComponent(imageLayout);
    initPicture();
  }
  
  protected void initPicture() {
    StreamResource imageresource = new StreamResource(new StreamSource() {
      private static final long serialVersionUID = 1L;
      public InputStream getStream() {
        return picture.getInputStream();
      }
    }, user.getId(), ExplorerApp.get());
    imageresource.setCacheTime(0);
    
    Embedded picture = new Embedded(null, imageresource);
    picture.setType(Embedded.TYPE_IMAGE);
    picture.setHeight(200, UNITS_PIXELS);
    picture.setWidth(200, UNITS_PIXELS);
    picture.addStyleName(ExplorerLayout.STYLE_PROFILE_PICTURE);
    
    imageLayout.addComponent(picture);
    imageLayout.setWidth(picture.getWidth() + 5, picture.getWidthUnits());
    
    // Change picture button
    if (isCurrentLoggedInUser) {
      Upload changePictureButton = initChangePictureButton();
      imageLayout.addComponent(changePictureButton);
      imageLayout.setComponentAlignment(changePictureButton, Alignment.MIDDLE_CENTER);
    }
  }
  
  protected Upload initChangePictureButton() {
    final Upload changePictureUpload = new Upload();
    changePictureUpload.setImmediate(true);
    changePictureUpload.setButtonCaption(i18nManager.getMessage(Messages.PROFILE_CHANGE_PICTURE));
    
    final InMemoryUploadReceiver receiver = initPictureReceiver(changePictureUpload);
    changePictureUpload.addListener(new FinishedListener() {
      private static final long serialVersionUID = 1L;
      public void uploadFinished(FinishedEvent event) {
        if (!receiver.isInterruped()) {
          picture = new Picture(receiver.getBytes(), receiver.getMimeType());
          identityService.setUserPicture(userId, picture);
          
          // reset picture
          imageLayout.removeAllComponents();
          initPicture();
        } else {
          receiver.reset();
        }
      }
    });
    
    return changePictureUpload;
  }
  
  protected InMemoryUploadReceiver initPictureReceiver(Upload upload) {
    InMemoryUploadReceiver receiver = new InMemoryUploadReceiver(upload, 102400L); // 100 kb limit
    upload.setReceiver(receiver);
    receiver.setAcceptedMimeTypes(Constants.DEFAULT_IMAGE_MIMETYPES);
    return receiver;
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
    
  }

  protected void initAboutSection() {
    // Header
    HorizontalLayout header = new HorizontalLayout();
    header.setWidth(100, UNITS_PERCENTAGE);
    header.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    infoPanelLayout.addComponent(header);
    
    Label aboutLabel = createProfileHeader(infoPanelLayout, i18nManager.getMessage(Messages.PROFILE_ABOUT));
    header.addComponent(aboutLabel);
    header.setExpandRatio(aboutLabel, 1.0f);
    
    // only show edit/save buttons if current user matches
    if (isCurrentLoggedInUser) {
      Button actionButton = null;
      if (!editable) {
        actionButton = initEditProfileButton();
      } else {
        actionButton = initSaveProfileButton();
      }
      header.addComponent(actionButton);
      header.setComponentAlignment(actionButton, Alignment.MIDDLE_RIGHT);
    }
    
    // 'About' fields
    GridLayout aboutLayout = createInfoSectionLayout(2, 4); 
    
    // Name
    if (!editable && (isDefined(user.getFirstName()) || isDefined(user.getLastName()) )) {
      addProfileEntry(aboutLayout, i18nManager.getMessage(Messages.PROFILE_NAME), user.getFirstName() + " " + user.getLastName());
    } else if (editable) {
      firstNameField = new TextField();
      firstNameField.focus();
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_FIRST_NAME), firstNameField, user.getFirstName());
      lastNameField = new TextField();
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_LAST_NAME), lastNameField, user.getLastName());
    }
    
    // Job title
    if (!editable && isDefined(jobTitle)) {
      addProfileEntry(aboutLayout, i18nManager.getMessage(Messages.PROFILE_JOBTITLE), jobTitle);
    } else if (editable) {
      jobTitleField = new TextField();
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_JOBTITLE), jobTitleField, jobTitle);
    }
    
    // Birthdate
    if (!editable && isDefined(birthDate)) {
      addProfileEntry(aboutLayout, i18nManager.getMessage(Messages.PROFILE_BIRTHDATE), birthDate);
    } else if (editable) {
      birthDateField = new DateField();
      birthDateField.setDateFormat(Constants.DEFAULT_DATE_FORMAT);
      birthDateField.setResolution(DateField.RESOLUTION_DAY);
      try {
        birthDateField.setValue(new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).parse(birthDate));
      } catch (Exception e) {} // do nothing
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_BIRTHDATE), birthDateField, null);
    }
    
    // Location
    if (!editable && isDefined(location)) {
      addProfileEntry(aboutLayout, i18nManager.getMessage(Messages.PROFILE_LOCATION), location);
    } else if (editable) {
      locationField = new TextField();
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_LOCATION), locationField, location);
    }
  }
  
  protected Button initEditProfileButton() {
    Button editProfileButton = new Button(i18nManager.getMessage(Messages.PROFILE_EDIT));
    editProfileButton.setIcon(Images.EDIT);
    editProfileButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        editable = true;
        initUi();
      }
    });
    return editProfileButton;
  }
  
  protected Button initSaveProfileButton() {
    Button saveProfileButton = new Button(i18nManager.getMessage(Messages.PROFILE_SAVE));
    saveProfileButton.setIcon(Images.SAVE);
    saveProfileButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        user.setFirstName((String) firstNameField.getValue());
        user.setLastName((String) lastNameField.getValue());
        user.setEmail((String) emailField.getValue());
        identityService.saveUser(user);
        
        identityService.setUserInfo(user.getId(), Constants.USER_INFO_JOB_TITLE, jobTitleField.getValue().toString());
        if (birthDateField.getValue() != null && !"".equals(birthDateField.getValue().toString())) {
          identityService.setUserInfo(user.getId(), Constants.USER_INFO_BIRTH_DATE, new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(birthDateField.getValue()));
        }
        identityService.setUserInfo(user.getId(), Constants.USER_INFO_LOCATION, locationField.getValue().toString());
        identityService.setUserInfo(user.getId(), Constants.USER_INFO_PHONE, phoneField.getValue().toString());
        identityService.setUserInfo(user.getId(), Constants.USER_INFO_TWITTER, twitterField.getValue().toString());
        identityService.setUserInfo(user.getId(), Constants.USER_INFO_SKYPE, skypeField.getValue().toString());
        
        // UI
        editable = false;
        loadProfileData();
        initUi();
        
        // Update user cache
        ExplorerApp.get().getUserCache().notifyUserDataChanged(user.getId());
      }
    });
    return saveProfileButton;
  }
  
  protected void initContactSection() {
    Label header = createProfileHeader(infoPanelLayout, i18nManager.getMessage(Messages.PROFILE_CONTACT));
    header.addStyleName(ExplorerLayout.STYLE_H3);
    header.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    infoPanelLayout.addComponent(header);
    
    GridLayout contactLayout = createInfoSectionLayout(2, 4);
    // Email
    if (!editable && isDefined(user.getEmail())) {
      addProfileEntry(contactLayout, i18nManager.getMessage(Messages.PROFILE_EMAIL), user.getEmail());
    } else if (editable) {
      emailField = new TextField();
      addProfileInputField(contactLayout, i18nManager.getMessage(Messages.PROFILE_EMAIL), emailField, user.getEmail());
    }
    
    // Phone
    if (!editable && isDefined(phone)) {
      addProfileEntry(contactLayout, i18nManager.getMessage(Messages.PROFILE_PHONE), phone);
    } else if (editable) {
      phoneField = new TextField();
      addProfileInputField(contactLayout, i18nManager.getMessage(Messages.PROFILE_PHONE), phoneField, phone);
    }
    
    // Twitter
    if (!editable && isDefined(twitterName)) {
      Link twitterLink = new Link(twitterName, new ExternalResource("http://www.twitter.com/"+twitterName)); 
      addProfileEntry(contactLayout, i18nManager.getMessage(Messages.PROFILE_TWITTER), twitterLink);
    } else if (editable) {
      twitterField = new TextField();
      addProfileInputField(contactLayout, i18nManager.getMessage(Messages.PROFILE_TWITTER), twitterField, twitterName);
    }
    
    // Skype
    if (!editable && isDefined(skypeId)) {
      // The skype entry shows the name + skype icon, laid out in a small grid
      GridLayout skypeLayout = new GridLayout(2,1);
      skypeLayout.setSpacing(true);
      skypeLayout.setSizeUndefined();
      
      Label skypeIdLabel = new Label(skypeId);
      skypeIdLabel.setSizeUndefined();
      skypeLayout.addComponent(skypeIdLabel);
      
      addProfileEntry(contactLayout, i18nManager.getMessage(Messages.PROFILE_SKYPE), skypeLayout);
    } else if (editable) {
      skypeField = new TextField();
      addProfileInputField(contactLayout, i18nManager.getMessage(Messages.PROFILE_SKYPE), skypeField, skypeId);
    }
  }
  
  protected boolean isDefined(String information) {
    return information != null && !"".equals(information);
  }
  
  protected Label createProfileHeader(VerticalLayout infoLayout, String headerName) {
    Label label = new Label(headerName);
    label.setWidth(100, UNITS_PERCENTAGE);
    label.addStyleName(ExplorerLayout.STYLE_H3);
    return label;
  }
  
  protected GridLayout createInfoSectionLayout(int columns, int rows) {
    GridLayout layout = new GridLayout(columns, rows);
    layout.setSpacing(true);
    layout.setWidth(100, UNITS_PERCENTAGE);
    layout.setMargin(true, false, true, false);
    infoPanelLayout.addComponent(layout);
    return layout;
  }
  
  protected void addProfileEntry(GridLayout layout, String name, String value) {
    addProfileEntry(layout, name, new Label(value));
  }
  
  protected void addProfileEntry(GridLayout layout, String name, Component value) {
    addProfileEntry(layout, new Label(name + ": "), value);
  }
  
  protected void addProfileEntry(GridLayout layout, Component name, Component value) {
    name.addStyleName(ExplorerLayout.STYLE_PROFILE_FIELD);
    name.setSizeUndefined();
    layout.addComponent(name);
    
    value.setSizeUndefined();
    layout.addComponent(value);
  }
  
  protected void addProfileInputField(GridLayout layout, String name, AbstractField inputField, String inputFieldValue) {
    Label label = new Label(name + ": ");
    label.addStyleName(ExplorerLayout.STYLE_PROFILE_FIELD);
    label.setSizeUndefined();
    layout.addComponent(label);
    layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
    
    if (inputFieldValue != null) {
      inputField.setValue(inputFieldValue);
    }
    layout.addComponent(inputField);
    layout.setComponentAlignment(inputField, Alignment.MIDDLE_LEFT);
  }
  
}
