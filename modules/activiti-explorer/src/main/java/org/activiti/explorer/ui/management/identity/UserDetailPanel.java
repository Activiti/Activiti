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

package org.activiti.explorer.ui.management.identity;

import java.io.InputStream;
import java.util.Set;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.ConfirmationDialogPopupWindow;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.event.ConfirmationEvent;
import org.activiti.explorer.ui.event.ConfirmationEventListener;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.event.SubmitEventListener;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

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
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class UserDetailPanel extends DetailPanel implements MemberShipChangeListener {

  private static final long serialVersionUID = 1L;
  
  protected transient IdentityService identityService;
  protected I18nManager i18nManager;
  
  protected UserPage userPage;
  protected User user;
  
  protected boolean editingDetails;
  protected HorizontalLayout userDetailsLayout;
  protected TextField firstNameField;
  protected TextField lastNameField;
  protected TextField emailField;
  protected PasswordField passwordField;
  protected HorizontalLayout groupLayout;
  protected Table groupTable;
  protected LazyLoadingContainer groupContainer;
  protected GroupsForUserQuery groupsForUserQuery;
  protected Label noGroupsLabel;
  
  public UserDetailPanel(UserPage userPage, String userId) {
    this.userPage = userPage;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.user = identityService.createUserQuery().userId(userId).singleResult();
    
    init();
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.PANEL_LIGHT);
    
    initPageTitle();
    initUserDetails();
    initGroups();
    
    initActions();
  }
  
  protected void initActions() {
    Button createUserButton = new Button(i18nManager.getMessage(Messages.USER_CREATE));
    createUserButton.setIcon(Images.USER_16);
    
    createUserButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        NewUserPopupWindow newUserPopupWindow = new NewUserPopupWindow();
        ExplorerApp.get().getViewManager().showPopupWindow(newUserPopupWindow);
      }
    });
    
    userPage.getToolBar().removeAllButtons();
    userPage.getToolBar().addButton(createUserButton);
  }

  protected void initPageTitle() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidth(100, UNITS_PERCENTAGE);
    layout.setSpacing(true);
    layout.setMargin(false, false, true, false);
    layout.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    addDetailComponent(layout);
    
    Embedded userImage = new Embedded(null, Images.USER_50);
    layout.addComponent(userImage);
    
    Label userName = new Label(user.getFirstName() + " " + user.getLastName());
    userName.setSizeUndefined();
    userName.addStyleName(Reindeer.LABEL_H2);
    layout.addComponent(userName);
    layout.setComponentAlignment(userName, Alignment.MIDDLE_LEFT);
    layout.setExpandRatio(userName, 1.0f);
  }
  
  protected void initUserDetails() {
    Label userDetailsHeader = new Label(i18nManager.getMessage(Messages.USER_HEADER_DETAILS));
    userDetailsHeader.addStyleName(ExplorerLayout.STYLE_H3);
    userDetailsHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(userDetailsHeader);
    
    // Details: picture and basic info
    userDetailsLayout = new HorizontalLayout();
    userDetailsLayout.setSpacing(true);
    userDetailsLayout.setMargin(false, false, true, false);
    addDetailComponent(userDetailsLayout);
    
    populateUserDetails();
  }
  
  protected void populateUserDetails() {
    loadPicture();
    loadUserDetails();
    initDetailsActions();
  }

  protected void loadPicture() {
    Component pictureComponent = null;
    final Picture userPicture = identityService.getUserPicture(user.getId());
    if (userPicture != null) {
      StreamResource imageresource = new StreamResource(new StreamSource() {
        private static final long serialVersionUID = 1L;
        public InputStream getStream() {
          return userPicture.getInputStream();
        }
      }, user.getId() + "." + Constants.MIMETYPE_EXTENSION_MAPPING.get(userPicture.getMimeType()), ExplorerApp.get());
      pictureComponent = new Embedded(null, imageresource);
    } else {
      pictureComponent = new Label("");
    }
    pictureComponent.setHeight("200px");
    pictureComponent.setWidth("200px");
    pictureComponent.addStyleName(ExplorerLayout.STYLE_PROFILE_PICTURE);
    userDetailsLayout.addComponent(pictureComponent);
    userDetailsLayout.setComponentAlignment(pictureComponent, Alignment.MIDDLE_CENTER);
  }
  
  protected void loadUserDetails() {
    // Grid of details
    GridLayout detailGrid = new GridLayout();
    detailGrid.setColumns(2);
    detailGrid.setSpacing(true);
    detailGrid.setMargin(true, true, false, true);
    userDetailsLayout.addComponent(detailGrid);
    
    // Details
    addUserDetail(detailGrid, i18nManager.getMessage(Messages.USER_ID), new Label(user.getId())); // details are non-editable
    if (!editingDetails) {
      addUserDetail(detailGrid, i18nManager.getMessage(Messages.USER_FIRSTNAME), new Label(user.getFirstName()));
      addUserDetail(detailGrid, i18nManager.getMessage(Messages.USER_LASTNAME), new Label(user.getLastName()));
      addUserDetail(detailGrid, i18nManager.getMessage(Messages.USER_EMAIL), new Label(user.getEmail()));
    } else {
      firstNameField = new TextField(null, user.getFirstName() != null ? user.getFirstName() : "");
      addUserDetail(detailGrid, i18nManager.getMessage(Messages.USER_FIRSTNAME), firstNameField);
      firstNameField.focus();
      
      lastNameField = new TextField(null, user.getLastName() != null ? user.getLastName() : "");
      addUserDetail(detailGrid, i18nManager.getMessage(Messages.USER_LASTNAME), lastNameField);
      
      emailField = new TextField(null, user.getEmail() != null ? user.getEmail() : "");
      addUserDetail(detailGrid, i18nManager.getMessage(Messages.USER_EMAIL), emailField);
      
      passwordField = new PasswordField();
      Label cautionLabel = new Label(i18nManager.getMessage(Messages.USER_RESET_PASSWORD));
      cautionLabel.addStyleName(Reindeer.LABEL_SMALL);
      HorizontalLayout passwordLayout = new HorizontalLayout();
      passwordLayout.setSpacing(true);
      passwordLayout.addComponent(passwordField);
      passwordLayout.addComponent(cautionLabel);
      passwordLayout.setComponentAlignment(cautionLabel, Alignment.MIDDLE_LEFT);
      addUserDetail(detailGrid, i18nManager.getMessage(Messages.USER_PASSWORD), passwordLayout);
    }
  }
  
  protected void addUserDetail(GridLayout detailLayout, String detail, Component value) {
    Label label = new Label(detail + ": ");
    label.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    detailLayout.addComponent(label);
    detailLayout.addComponent(value);
  }
  
  protected void initDetailsActions() {
    VerticalLayout actionLayout = new VerticalLayout();
    actionLayout.setSpacing(true);
    actionLayout.setMargin(false, false, false, true);
    userDetailsLayout.addComponent(actionLayout);
    
    if (!editingDetails) {
      initEditButton(actionLayout);
      initDeleteButton(actionLayout);
    } else {
      initSaveButton(actionLayout);
    }
  }

  protected void initEditButton(VerticalLayout actionLayout) {
    Button editButton = new Button(i18nManager.getMessage(Messages.USER_EDIT));
    editButton.addStyleName(Reindeer.BUTTON_SMALL);
    actionLayout.addComponent(editButton);
    editButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        editingDetails = true;
        userDetailsLayout.removeAllComponents();
        populateUserDetails(); // the layout will be populated differently since the 'editingDetails' boolean is set
      }
    });
  }
  
  protected void initSaveButton(VerticalLayout actionLayout) {
    Button saveButton = new Button(i18nManager.getMessage(Messages.USER_SAVE));
    saveButton.addStyleName(Reindeer.BUTTON_SMALL);
    actionLayout.addComponent(saveButton);
    saveButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        
        String originalFirstName = user.getFirstName();
        String originalLastName = user.getLastName();
        
        // Change data
        user.setFirstName(firstNameField.getValue().toString());
        user.setLastName(lastNameField.getValue().toString());
        user.setEmail(emailField.getValue().toString());
        if (passwordField.getValue() != null && !"".equals(passwordField.getValue().toString())) {
          user.setPassword(passwordField.getValue().toString());
        }
        identityService.saveUser(user);
        
        // Refresh detail panel
        editingDetails = false;
        userDetailsLayout.removeAllComponents();
        populateUserDetails();
        
       // Refresh task list (only if name was changed)
       if (nameChanged(originalFirstName, originalLastName)) {
         userPage.notifyUserChanged(user.getId());
       }
      }
    });
  }
  
  protected boolean nameChanged(String originalFirstName, String originalLastName) {
    boolean nameChanged = false;
    if (originalFirstName != null) {
      nameChanged = !originalFirstName.equals(user.getFirstName());
    } else {
      nameChanged = user.getFirstName() != null;
    }
    
    if (!nameChanged) {
      if (originalLastName != null) {
        nameChanged = !originalLastName.equals(user.getLastName());
      } else {
        nameChanged = user.getLastName() != null;
      }
    }
    return nameChanged;
  }
  
  protected void initDeleteButton(VerticalLayout actionLayout) {
    Button deleteButton = new Button(i18nManager.getMessage(Messages.USER_DELETE));
    deleteButton.addStyleName(Reindeer.BUTTON_SMALL);
    actionLayout.addComponent(deleteButton);
    deleteButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ConfirmationDialogPopupWindow confirmPopup = 
          new ConfirmationDialogPopupWindow(i18nManager.getMessage(Messages.USER_CONFIRM_DELETE, user.getId()));
        
        confirmPopup.addListener(new ConfirmationEventListener() {
          protected void rejected(ConfirmationEvent event) {
          }
          protected void confirmed(ConfirmationEvent event) {
            // Delete user from database
            identityService.deleteUser(user.getId());

            // Update ui
            userPage.refreshSelectNext();
          }
        });
        
        ExplorerApp.get().getViewManager().showPopupWindow(confirmPopup);
      }
    });
  }
  
  protected void initGroups() {
    HorizontalLayout groupHeader = new HorizontalLayout();
    groupHeader.setWidth(100, UNITS_PERCENTAGE);
    groupHeader.setSpacing(true);
    groupHeader.setMargin(false, false, true, false);
    groupHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(groupHeader);
    
    initGroupTitle(groupHeader);
    initAddGroupsButton(groupHeader);
    
    groupLayout = new HorizontalLayout(); // we wrap the table in a simple layout so we can remove the table easy later on
    groupLayout.setWidth(100, UNITS_PERCENTAGE);
    addDetailComponent(groupLayout);
    initGroupsTable();
  }

  protected void initGroupTitle(HorizontalLayout groupHeader) {
    Label groupsTitle = new Label(i18nManager.getMessage(Messages.USER_HEADER_GROUPS));
    groupsTitle.addStyleName(ExplorerLayout.STYLE_H3);
    groupHeader.addComponent(groupsTitle);
  }

  protected void initAddGroupsButton(HorizontalLayout groupHeader) {
    Button addRelatedContentButton = new Button();
    addRelatedContentButton.addStyleName(ExplorerLayout.STYLE_ADD);
    groupHeader.addComponent(addRelatedContentButton);
    groupHeader.setComponentAlignment(addRelatedContentButton, Alignment.MIDDLE_RIGHT);
    
    addRelatedContentButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        final GroupSelectionPopupWindow selectionPopup = new GroupSelectionPopupWindow(identityService, user.getId());
        selectionPopup.addListener(new SubmitEventListener() {
          private static final long serialVersionUID = 1L;
          protected void submitted(SubmitEvent event) {
            Set<String> selectedGroups = selectionPopup.getSelectedGroupIds();
            if (!selectedGroups.isEmpty()) {
              for (String groupId : selectedGroups) {
                identityService.createMembership(user.getId(), groupId);
              }
              notifyMembershipChanged();
            }
          }
          protected void cancelled(SubmitEvent event) {
          }
        });
        ExplorerApp.get().getViewManager().showPopupWindow(selectionPopup);
      }
    });
  }
  
  protected void initGroupsTable() {
    groupsForUserQuery = new GroupsForUserQuery(identityService, this, user.getId());
    if (groupsForUserQuery.size() > 0) {
      groupTable = new Table();
      groupTable.setSortDisabled(true);
      groupTable.setHeight(150, UNITS_PIXELS);
      groupTable.setWidth(100, UNITS_PERCENTAGE);
      groupLayout.addComponent(groupTable);
      
      groupContainer = new LazyLoadingContainer(groupsForUserQuery, 30);
      groupTable.setContainerDataSource(groupContainer);
      
      groupTable.addContainerProperty("id", Button.class, null);
      groupTable.setColumnExpandRatio("id", 22);
      groupTable.addContainerProperty("name", String.class, null);
      groupTable.setColumnExpandRatio("name", 45);
      groupTable.addContainerProperty("type", String.class, null);
      groupTable.setColumnExpandRatio("type", 22);
      groupTable.addContainerProperty("actions", Component.class, null);
      groupTable.setColumnExpandRatio("actions", 11);
      groupTable.setColumnAlignment("actions", Table.ALIGN_CENTER);

    } else {
      noGroupsLabel = new Label(i18nManager.getMessage(Messages.USER_NO_GROUPS));
      groupLayout.addComponent(noGroupsLabel);
    }
  }
  
  public void notifyMembershipChanged() {
    groupLayout.removeAllComponents();
    initGroupsTable();
  }
  
}
