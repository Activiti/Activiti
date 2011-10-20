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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.ConfirmationDialogPopupWindow;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.SelectUsersPopupWindow;
import org.activiti.explorer.ui.event.ConfirmationEvent;
import org.activiti.explorer.ui.event.ConfirmationEventListener;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.event.SubmitEventListener;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class GroupDetailPanel extends DetailPanel implements MemberShipChangeListener {

  private static final long serialVersionUID = 1L;
  
  protected IdentityService identityService;
  protected I18nManager i18nManager;

  protected GroupPage groupPage;
  protected Group group; 
  protected VerticalLayout panelLayout;
  protected boolean editingDetails;
  protected HorizontalLayout detailLayout;
  protected GridLayout detailsGrid;
  protected TextField nameTextField;
  protected ComboBox typeCombobox;
  protected HorizontalLayout membersLayout;
  protected Table membersTable;
  protected Label noMembersTable;
  
  public GroupDetailPanel(GroupPage groupPage, String groupId) {
    this.groupPage = groupPage;
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.group = identityService.createGroupQuery().groupId(groupId).singleResult();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    init();
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.PANEL_LIGHT);
    
    initPageTitle();
    initGroupDetails();
    initMembers();
    
    initActions();
  }
  
  protected void initActions() {
    Button createGroupButton = new Button(i18nManager.getMessage(Messages.GROUP_CREATE));
    createGroupButton.setIcon(Images.GROUP_16);
    createGroupButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        NewGroupPopupWindow popup = new NewGroupPopupWindow();
        ExplorerApp.get().getViewManager().showPopupWindow(popup);
      }
    });
    groupPage.getToolBar().removeAllButtons();
    groupPage.getToolBar().addButton(createGroupButton);
  }

  protected void initPageTitle() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidth(100, UNITS_PERCENTAGE);
    layout.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    layout.setSpacing(true);
    layout.setMargin(false, false, true, false);
    addDetailComponent(layout);
    
    Embedded groupImage = new Embedded(null, Images.GROUP_50);
    layout.addComponent(groupImage);
    
    Label groupName = new Label(getGroupName(group));
    groupName.setSizeUndefined();
    groupName.addStyleName(Reindeer.LABEL_H2);
    layout.addComponent(groupName);
    layout.setComponentAlignment(groupName, Alignment.MIDDLE_LEFT);
    layout.setExpandRatio(groupName, 1.0f);
  }
  
  protected String getGroupName(Group theGroup) {
    if(theGroup.getName() == null) {
      return theGroup.getId();
    }
    return group.getName();
  }

  protected void initGroupDetails() {
    Label groupDetailsHeader = new Label(i18nManager.getMessage(Messages.GROUP_HEADER_DETAILS));
    groupDetailsHeader.addStyleName(ExplorerLayout.STYLE_H3);
    groupDetailsHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    
    addDetailComponent(groupDetailsHeader);
    
    detailLayout = new HorizontalLayout();
    detailLayout.setSpacing(true);
    detailLayout.setMargin(true, false, true, false);
    addDetailComponent(detailLayout);
    
    populateGroupDetails();
  }
  
  protected void populateGroupDetails() {
    initGroupProperties();
    initGroupActions();
  }
  
  protected void initGroupProperties() {
    detailsGrid = new GridLayout(2, 3);
    detailsGrid.setSpacing(true);
    detailLayout.setMargin(true, true, true, false);
    detailLayout.addComponent(detailsGrid);
    
    // id
    Label idLabel = new Label(i18nManager.getMessage(Messages.GROUP_ID) + ": ");
    idLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    detailsGrid.addComponent(idLabel);
    Label idValueLabel = new Label(group.getId());
    detailsGrid.addComponent(idValueLabel);
    
    // name
    Label nameLabel = new Label(i18nManager.getMessage(Messages.GROUP_NAME) + ": ");
    nameLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    detailsGrid.addComponent(nameLabel);
    if (!editingDetails) {
      Label nameValueLabel = new Label(group.getName());
      detailsGrid.addComponent(nameValueLabel);
    } else {
      nameTextField = new TextField(null, group.getName());
      detailsGrid.addComponent(nameTextField);
    }
    
    // Type
    Label typeLabel = new Label(i18nManager.getMessage(Messages.GROUP_TYPE) + ": ");
    typeLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    detailsGrid.addComponent(typeLabel);
    if (!editingDetails) {
      Label typeValueLabel = new Label(group.getType());
      detailsGrid.addComponent(typeValueLabel);
    } else {
      typeCombobox = new ComboBox(null, Arrays.asList("assignment", "security-role"));
      typeCombobox.setNullSelectionAllowed(false);
      typeCombobox.setInvalidAllowed(false);
      typeCombobox.select(group.getType());
      detailsGrid.addComponent(typeCombobox);
    }
  }
  
  protected void initGroupActions() {
    VerticalLayout actionsLayout = new VerticalLayout();
    actionsLayout.setSpacing(true);
    actionsLayout.setMargin(false, false, false, true);
    detailLayout.addComponent(actionsLayout);
    
    if (editingDetails) {
      initSaveButton(actionsLayout);
    } else {
      initEditButton(actionsLayout);
      initDeleteButton(actionsLayout);
    }
  }
  
  protected void initEditButton(VerticalLayout actionsLayout) {
    Button editButton = new Button(i18nManager.getMessage(Messages.USER_EDIT));
    editButton.addStyleName(Reindeer.BUTTON_SMALL);
    actionsLayout.addComponent(editButton);
    
    editButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        editingDetails = true;
        detailLayout.removeAllComponents();
        populateGroupDetails();
      }
    });
  }
  
  protected void initSaveButton(VerticalLayout actionsLayout) {
    Button saveButton = new Button(i18nManager.getMessage(Messages.USER_SAVE));
    saveButton.addStyleName(Reindeer.BUTTON_SMALL);
    actionsLayout.addComponent(saveButton);
    
    saveButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        String originalName = group.getName();
        
        // Update data
        if (nameTextField.getValue() != null) {
        group.setName(nameTextField.getValue().toString());
        group.setType(typeCombobox.getValue().toString());
        }
        identityService.saveGroup(group);
        
        // Update UI
        editingDetails = false;
        detailLayout.removeAllComponents();
        populateGroupDetails();
        
        // Refresh task list (only if name was changed)
        if ( (originalName != null && !originalName.equals(group.getName())) 
                || (originalName == null && group.getName() != null)) {
          groupPage.notifyGroupChanged(group.getId());
        }
      }
    });
  }
  
  protected void initDeleteButton(VerticalLayout actionsLayout) {
    Button deleteButton = new Button(i18nManager.getMessage(Messages.GROUP_DELETE));
    deleteButton.addStyleName(Reindeer.BUTTON_SMALL);
    actionsLayout.addComponent(deleteButton);
    
    deleteButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ConfirmationDialogPopupWindow confirmPopup = 
          new ConfirmationDialogPopupWindow(i18nManager.getMessage(Messages.GROUP_CONFIRM_DELETE, group.getId()));
        confirmPopup.addListener(new ConfirmationEventListener() {
          protected void rejected(ConfirmationEvent event) {
          }
          protected void confirmed(ConfirmationEvent event) {
            // Delete group from database
            identityService.deleteGroup(group.getId());

            // Update ui
            groupPage.refreshSelectNext();
          }
        });
        
        ExplorerApp.get().getViewManager().showPopupWindow(confirmPopup);
      }
    });
  }
  
  protected void initMembers() {
    HorizontalLayout membersHeader = new HorizontalLayout();
    membersHeader.setSpacing(true);
    membersHeader.setWidth(100, UNITS_PERCENTAGE);
    membersHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(membersHeader);
    
    initMembersTitle(membersHeader);
    initAddMembersButton(membersHeader);
    
    membersLayout = new HorizontalLayout();
    membersLayout.setWidth(100, UNITS_PERCENTAGE);
    addDetailComponent(membersLayout);
    initMembersTable();
  }
  
  protected void initMembersTitle(HorizontalLayout membersHeader) {
    Label usersHeader = new Label(i18nManager.getMessage(Messages.GROUP_HEADER_USERS));
    usersHeader.addStyleName(ExplorerLayout.STYLE_H3);
    membersHeader.addComponent(usersHeader);
  }
  
  protected void initAddMembersButton(HorizontalLayout membersHeader) {
    Button addButton = new Button();
    addButton.addStyleName(ExplorerLayout.STYLE_ADD);
    membersHeader.addComponent(addButton);
    membersHeader.setComponentAlignment(addButton, Alignment.MIDDLE_RIGHT);
    
    addButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        final SelectUsersPopupWindow selectUsersPopup =  new SelectUsersPopupWindow(
                i18nManager.getMessage(Messages.GROUP_SELECT_MEMBERS, group.getId()), 
                true, false, getCurrentMembers());
        ExplorerApp.get().getViewManager().showPopupWindow(selectUsersPopup);
        
        // Listen to submit events (that contain the selected users)
        selectUsersPopup.addListener(new SubmitEventListener() {
          protected void submitted(SubmitEvent event) {
            Collection<String> userIds = selectUsersPopup.getSelectedUserIds();
            if (userIds.size() > 0) {
              for (String userId : userIds) {
                identityService.createMembership(userId, group.getId());
              }
              notifyMembershipChanged();
            }
          }
          protected void cancelled(SubmitEvent event) {
          }
        });
      }
    });
  }
  
  // Hacky - must be put in custom service
  protected List<String> getCurrentMembers() {
    List<User> users = identityService.createUserQuery().memberOfGroup(group.getId()).list();
    List<String> userIds = new ArrayList<String>();
    for (User user : users) {
      userIds.add(user.getId());
    }
    return userIds;
  }
  
  protected void initMembersTable() {
    LazyLoadingQuery query = new GroupMembersQuery(group.getId(), this);
    if (query.size() > 0) {
      membersTable = new Table();
      membersTable.setWidth(100, UNITS_PERCENTAGE);
      membersTable.setHeight(400, UNITS_PIXELS);
      
      membersTable.setEditable(false);
      membersTable.setSelectable(false);
      membersTable.setSortDisabled(false);
      
      LazyLoadingContainer container = new LazyLoadingContainer(query, 10);
      membersTable.setContainerDataSource(container);
      
      membersTable.addContainerProperty("id", Button.class, null);
      membersTable.addContainerProperty("firstName", String.class, null);
      membersTable.addContainerProperty("lastName", String.class, null);
      membersTable.addContainerProperty("email", String.class, null);
      membersTable.addContainerProperty("actions", Component.class, null);
      
      membersLayout.addComponent(membersTable);
    } else {
      noMembersTable = new Label(i18nManager.getMessage(Messages.GROUP_NO_MEMBERS));
      membersLayout.addComponent(noMembersTable);
    }
  }
  
  public void notifyMembershipChanged() {
    membersLayout.removeAllComponents();
    initMembersTable();
  }

}
