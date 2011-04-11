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
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.InMemoryUploadReceiver;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.Receiver;
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
  protected boolean editable = false;
  protected HorizontalLayout profilePanelLayout;
  protected VerticalLayout imageLayout;
  protected VerticalLayout infoPanelLayout;
  protected TextField firstNameField;
  protected TextField lastNameField;
  protected PasswordField passwordField;
  protected TextField jobTitleField;
  protected TextField birthDateField;
  protected TextField locationField;
  protected TextField emailField;
  protected TextField phoneField;
  protected TextField twitterField;
  protected TextField skypeField;
  
  // keys for storing user info
  protected static final String KEY_BIRTH_DATE = "birthDate";
  protected static final String KEY_JOB_TITLE = "jobTitle";
  protected static final String KEY_LOCATION = "location";
  protected static final String KEY_PHONE = "phone";
  protected static final String KEY_TWITTER = "twitterName";
  protected static final String KEY_SKYPE = "skype";
  
  public ProfilePanel(String userId) {
    this.userId = userId;
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    loadProfileData();
    initUi();
  }
  
  protected void loadProfileData() {
    this.user = identityService.createUserQuery().userId(userId).singleResult();
    this.picture = identityService.getUserPicture(user.getId());
    this.birthDate = identityService.getUserInfo(user.getId(), KEY_BIRTH_DATE);
    this.jobTitle = identityService.getUserInfo(user.getId(), KEY_JOB_TITLE);
    this.location = identityService.getUserInfo(user.getId(), KEY_LOCATION);
    this.phone = identityService.getUserInfo(user.getId(), KEY_PHONE);
    this.twitterName = identityService.getUserInfo(user.getId(), KEY_TWITTER);
    this.skypeId = identityService.getUserInfo(user.getId(), KEY_SKYPE);
  }

  protected void initUi() {
    removeAllComponents();
    addStyleName(Reindeer.PANEL_LIGHT);
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
      private static final long serialVersionUID = -8875067466181823014L;
      public InputStream getStream() {
        return picture.getInputStream();
      }
    }, user.getId(), ExplorerApp.get());
    imageresource.setCacheTime(0);
    
    Embedded picture = new Embedded("", imageresource);
    picture.setType(Embedded.TYPE_IMAGE);
    picture.setHeight("200px");
    picture.setWidth("200px");
    picture.addStyleName(ExplorerLayout.STYLE_PROFILE_PICTURE);
    
    imageLayout.addComponent(picture);
    imageLayout.setWidth(picture.getWidth() + 5, picture.getWidthUnits());
    
    // Change picture button
    if (userId.equals(ExplorerApp.get().getLoggedInUser().getId())) {
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
    initAccountsSection();
  }

  protected void initAboutSection() {
    // Header
    HorizontalLayout header = new HorizontalLayout();
    header.setWidth(100, UNITS_PERCENTAGE);
    infoPanelLayout.addComponent(header);
    
    Label aboutLabel = createProfileHeader(infoPanelLayout, i18nManager.getMessage(Messages.PROFILE_ABOUT));
    header.addComponent(aboutLabel);
    header.setExpandRatio(aboutLabel, 1.0f);
    
    // only show edit/save buttons if current user matches
    if (userId.equals(ExplorerApp.get().getLoggedInUser().getId())) {
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
    
    if (!editable && (isDefined(user.getFirstName()) || isDefined(user.getLastName()) )) {
      addProfileEntry(aboutLayout, i18nManager.getMessage(Messages.PROFILE_NAME), user.getFirstName() + " " + user.getLastName());
    } else if (editable) {
      firstNameField = new TextField();
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_FIRST_NAME), firstNameField, user.getFirstName());
      lastNameField = new TextField();
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_LAST_NAME), lastNameField, user.getLastName());
    }
    
    if (!editable && isDefined(jobTitle)) {
      addProfileEntry(aboutLayout, i18nManager.getMessage(Messages.PROFILE_JOBTITLE), jobTitle);
    } else if (editable) {
      jobTitleField = new TextField();
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_JOBTITLE), jobTitleField, jobTitle);
    }
    
    if (!editable && isDefined(birthDate)) {
      addProfileEntry(aboutLayout, i18nManager.getMessage(Messages.PROFILE_BIRTHDATE), birthDate);
    } else if (editable) {
      birthDateField = new TextField();
      addProfileInputField(aboutLayout, i18nManager.getMessage(Messages.PROFILE_BIRTHDATE), birthDateField, birthDate);
    }
    
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
    editProfileButton.addStyleName(Reindeer.BUTTON_SMALL);
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
    saveProfileButton.addStyleName(Reindeer.BUTTON_SMALL);
    saveProfileButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        user.setFirstName((String) firstNameField.getValue());
        user.setLastName((String) lastNameField.getValue());
        user.setEmail((String) emailField.getValue());
        identityService.saveUser(user);
        
        identityService.setUserInfo(user.getId(), KEY_JOB_TITLE, (String) jobTitleField.getValue());
        identityService.setUserInfo(user.getId(), KEY_BIRTH_DATE, (String) birthDateField.getValue());
        identityService.setUserInfo(user.getId(), KEY_LOCATION, (String) locationField.getValue());
        identityService.setUserInfo(user.getId(), KEY_PHONE, (String) phoneField.getValue());
        identityService.setUserInfo(user.getId(), KEY_TWITTER, (String) twitterField.getValue());
        identityService.setUserInfo(user.getId(), KEY_SKYPE, (String) skypeField.getValue());
        
        // UI
        editable = false;
        loadProfileData();
        initUi();
      }
    });
    return saveProfileButton;
  }
  
  protected void initContactSection() {
    Label header = createProfileHeader(infoPanelLayout, i18nManager.getMessage(Messages.PROFILE_CONTACT));
    infoPanelLayout.addComponent(header);
    
    GridLayout contactLayout = createInfoSectionLayout(2, 4);
    
    if (!editable && isDefined(user.getEmail())) {
      addProfileEntry(contactLayout, i18nManager.getMessage(Messages.PROFILE_EMAIL), user.getEmail());
    } else if (editable) {
      emailField = new TextField();
      addProfileInputField(contactLayout, i18nManager.getMessage(Messages.PROFILE_EMAIL), emailField, user.getEmail());
    }
    
    if (!editable && isDefined(phone)) {
      addProfileEntry(contactLayout, i18nManager.getMessage(Messages.PROFILE_PHONE), phone);
    } else if (editable) {
      phoneField = new TextField();
      addProfileInputField(contactLayout, i18nManager.getMessage(Messages.PROFILE_PHONE), phoneField, phone);
    }
    
    if (!editable && isDefined(twitterName)) {
      addProfileEntry(contactLayout, i18nManager.getMessage(Messages.PROFILE_TWITTER), twitterName);
    } else if (editable) {
      twitterField = new TextField();
      addProfileInputField(contactLayout, i18nManager.getMessage(Messages.PROFILE_TWITTER), twitterField, twitterName);
    }
    
    if (!editable && isDefined(skypeId)) {
      // The skype entry shows the name + skype icon, laid out in a small grid
      GridLayout skypeLayout = new GridLayout(3,1);
      skypeLayout.setSizeUndefined();
      
      Label skypeIdLabel = new Label(skypeId);
      skypeIdLabel.setSizeUndefined();
      skypeLayout.addComponent(skypeIdLabel);
      
      Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
      emptySpace.setSizeUndefined();
      skypeLayout.addComponent(emptySpace);
      
      Embedded skypeImage = new Embedded(null, Images.SKYPE);
      skypeImage.setSizeUndefined();
      skypeLayout.addComponent(skypeImage);
      
      addProfileEntry(contactLayout, i18nManager.getMessage(Messages.PROFILE_SKYPE), skypeLayout);
    } else if (editable) {
      skypeField = new TextField();
      addProfileInputField(contactLayout, i18nManager.getMessage(Messages.PROFILE_SKYPE), skypeField, skypeId);
    }
  }
  
  protected boolean isDefined(String information) {
    return information != null && !"".equals(information);
  }
  
  protected void  initAccountsSection() {
    Label header = createProfileHeader(infoPanelLayout, i18nManager.getMessage(Messages.PROFILE_ACCOUNTS));
    infoPanelLayout.addComponent(header);
    
    GridLayout accountLayout = createInfoSectionLayout(3, 2); 

    // Google
    Embedded googleImage = new Embedded(null, Images.GOOGLE);
    googleImage.setSizeUndefined();
    accountLayout.addComponent(googleImage);
    addProfileEntry(accountLayout, "Google", "mr_kermit_frog@gmail.com");
    
    // Alfresco
    Embedded alfrescoImage = new Embedded(null, Images.ALFRESCO);
    alfrescoImage.setSizeUndefined();
    accountLayout.addComponent(alfrescoImage);
    addProfileEntry(accountLayout, "Alfresco", "kermit_alfresco");
  }
  
  protected Label createProfileHeader(VerticalLayout infoLayout, String headerName) {
    Label label = new Label(headerName);
    label.setWidth("50%");
    label.addStyleName(ExplorerLayout.STYLE_PROFILE_HEADER);
    return label;
  }
  
  protected GridLayout createInfoSectionLayout(int columns, int rows) {
    GridLayout layout = new GridLayout(columns, rows);
    layout.setSpacing(true);
    layout.addStyleName(ExplorerLayout.STYLE_PROFILE_LAYOUT);
    infoPanelLayout.addComponent(layout);
    return layout;
  }
  
  protected void addProfileEntry(GridLayout layout, String name, String value) {
    addProfileEntry(layout, name + ": ", new Label(value));
  }
  
  protected void addProfileEntry(GridLayout layout, String name, Component value) {
    addProfileEntry(layout, new Label(name), value);
  }
  
  protected void addProfileEntry(GridLayout layout, Component name, Component value) {
    name.addStyleName(ExplorerLayout.STYLE_PROFILE_FIELD);
    name.setSizeUndefined();
    layout.addComponent(name);
    
    value.setSizeUndefined();
    layout.addComponent(value);
  }
  
  protected void addProfileInputField(GridLayout layout, String name, TextField inputField, String inputFieldValue) {
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
