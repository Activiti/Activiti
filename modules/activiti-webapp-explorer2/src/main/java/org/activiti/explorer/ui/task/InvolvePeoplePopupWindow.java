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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.cache.UserCache;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class InvolvePeoplePopupWindow extends Window {

  private static final long serialVersionUID = 1L;
  
  protected Task task;
  
  protected UserCache userCache;
  protected I18nManager i18nManager;
  protected TaskService taskService;
  
  protected VerticalLayout windowLayout;
  protected TextField searchField;
  protected HorizontalLayout userSelectionLayout;
  protected Table matchingUsersTable;
  protected Button selectUserButton;
  protected Table selectedUsersTable;
  protected Button doneButton;
  
  public InvolvePeoplePopupWindow(Task task) {
    this.task = task;
    this.userCache = ExplorerApp.get().getUserCache();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    initUi();
  }
  
  protected void initUi() {
    setCaption(i18nManager.getMessage(Messages.PEOPLE_INVOLVE_POPUP_CAPTION));
    setModal(true);
    addStyleName(Reindeer.WINDOW_LIGHT);
    center();
    
    windowLayout = (VerticalLayout) getContent();
    windowLayout.setSpacing(true);
    
    setWidth(820, UNITS_PIXELS);
    setHeight(350, UNITS_PIXELS);

    initSearchField();
    initUserSelection();
    initDoneButton();
  }
  
  protected void initSearchField() {
    // textfield
    searchField = new TextField();
    searchField.setInputPrompt(i18nManager.getMessage(Messages.PEOPLE_SEARCH));
    searchField.setWidth(250, UNITS_PIXELS);
    searchField.focus();
    addComponent(searchField);
    
    // Logic to change table according to input
    searchField.addListener(new TextChangeListener() {
      public void textChange(TextChangeEvent event) {
        searchPeople(event.getText());
      }
    });
  }
  
  protected void searchPeople(String searchText) {
    if (searchText.length() >= 2) {
      matchingUsersTable.removeAllItems();
      List<User> results = userCache.findMatchingUsers(searchText);
      for (User user : results) {
        if (!selectedUsersTable.containsId(user.getId())) {
          Item item = matchingUsersTable.addItem(user.getId());
          item.getItemProperty("userName").setValue(user.getFirstName() + " " + user.getLastName());
        }
      }
    }
  }
  
  protected void initUserSelection() {
    userSelectionLayout = new HorizontalLayout();
    userSelectionLayout.setSpacing(true);
    addComponent(userSelectionLayout);
    
    initMatchingUsersTable();
    initSelectUserButton();
    initSelectedUsersTable();
  }
  
  protected void initMatchingUsersTable() {
   matchingUsersTable = new Table();
   matchingUsersTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
   matchingUsersTable.setSelectable(true);
   matchingUsersTable.setEditable(false);
   matchingUsersTable.setImmediate(true);
   matchingUsersTable.setMultiSelect(true);
   matchingUsersTable.setNullSelectionAllowed(false);
   matchingUsersTable.setSortDisabled(true);
   
   matchingUsersTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.USER));
   matchingUsersTable.setColumnWidth("icon", 16);
   matchingUsersTable.addContainerProperty("userName", String.class, null);

   matchingUsersTable.setWidth(300, UNITS_PIXELS);
   matchingUsersTable.setHeight(200, UNITS_PIXELS);
   userSelectionLayout.addComponent(matchingUsersTable);
  }
  
  protected void initSelectUserButton() {
    selectUserButton = new Button(">");
    
    selectUserButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        for (String selectedItemId : (Set<String>) matchingUsersTable.getValue()) {
          // Remove from left table
          Item originalItem = matchingUsersTable.getItem(selectedItemId);
          
          // And put it in right table
          selectUser(selectedItemId, (String) originalItem.getItemProperty("userName").getValue());
          
          // Remove from left table (must be done on the end, or item properties will be inaccessible) 
          matchingUsersTable.removeItem(selectedItemId);
        }
      }
    });
    
    userSelectionLayout.addComponent(selectUserButton);
    userSelectionLayout.setComponentAlignment(selectUserButton, Alignment.MIDDLE_CENTER);
  }
  
  protected void initSelectedUsersTable() {
    selectedUsersTable = new Table();
    selectedUsersTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    selectedUsersTable.setEditable(false);
    selectedUsersTable.setSortDisabled(true);
    
    // Icon column
    selectedUsersTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.USER_ADD));
    selectedUsersTable.setColumnWidth("icon", 16);
    
    // Name column
    selectedUsersTable.addContainerProperty("userName", String.class, null);
    
    // Role column
    selectedUsersTable.addContainerProperty("role", ComboBox.class, null);
    
    // Delete icon column
    selectedUsersTable.addGeneratedColumn("delete", new ThemeImageColumnGenerator(Images.DELETE, 
      new com.vaadin.event.MouseEvents.ClickListener() {
        public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
          Object itemId = ((Embedded) event.getSource()).getData();
          
          // Add to left table (if possible)
          String searchFieldValue = (String) searchField.getValue();
          if (searchFieldValue != null && searchFieldValue.length() >= 2) {
            String userName = (String) selectedUsersTable.getItem(itemId).getItemProperty("userName").getValue();
            if (matchesSearchField(userName)) {
              Item item = matchingUsersTable.addItem(itemId);
              item.getItemProperty("userName").setValue(userName);
            }
          }
            
          // Delete from right table
          selectedUsersTable.removeItem(itemId);
        }
    }));
    selectedUsersTable.setColumnWidth("icon", 16);

    selectedUsersTable.setWidth(420, UNITS_PIXELS);
    selectedUsersTable.setHeight(200, UNITS_PIXELS);
    userSelectionLayout.addComponent(selectedUsersTable);
  }
  
  protected boolean matchesSearchField(String text) {
    for (String userNameToken : text.split(" ")) {
      if (userNameToken.toLowerCase().startsWith(((String) searchField.getValue()).toLowerCase())) {
        return true;
      }
    }
    return false;
  }
  
  @SuppressWarnings("unchecked")
  protected void initDoneButton() {
    doneButton = new Button("Done");
    
    doneButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        // create identitylinks for each selected user
        Collection<String> selectedUserIds = (Collection<String>) selectedUsersTable.getItemIds();
        for (String userId : selectedUserIds) {
          String role = (String) ((ComboBox) selectedUsersTable.getItem(userId).getItemProperty("role").getValue()).getValue();
          taskService.addUserIdentityLink(task.getId(), userId, role);
        }
        
        // close popup window
        close();
        
        // Fire event such that task details panel can be updated
        if (selectedUserIds.size() > 0) {
          fireEvent(new SubmitEvent(doneButton, SubmitEvent.SUBMITTED));
        }
      }
    });
    
    addComponent(doneButton);
    windowLayout.setComponentAlignment(doneButton, Alignment.MIDDLE_RIGHT);
  }
  
  protected void selectUser(String userId, String userName) {
    Item item = selectedUsersTable.addItem(userId);
    item.getItemProperty("userName").setValue(userName);
    ComboBox comboBox = new ComboBox(null, Arrays.asList(
            i18nManager.getMessage(Messages.TASK_ROLE_CONTRIBUTOR),
            i18nManager.getMessage(Messages.TASK_ROLE_IMPLEMENTER),
            i18nManager.getMessage(Messages.TASK_ROLE_MANAGER),
            i18nManager.getMessage(Messages.TASK_ROLE_SPONSOR)));
    comboBox.select(i18nManager.getMessage(Messages.TASK_ROLE_CONTRIBUTOR));
    comboBox.setNewItemsAllowed(true);
    item.getItemProperty("role").setValue(comboBox);
  }
  
}
