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

import java.util.List;

import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.cache.UserCache;

import com.vaadin.data.Item;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
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
  
  protected UserCache userCache;
  protected I18nManager i18nManager;
  
  protected VerticalLayout windowLayout;
  protected TextField searchField;
  protected HorizontalLayout userSelectionLayout;
  protected Table matchingUsersTable;
  protected Button selectUserButton;
  protected Table selectedUsersTable;
  protected Button doneButton;
  
  public InvolvePeoplePopupWindow() {
    this.userCache = UserCache.getInstance(); // TODO: see UserCache, probably will be refactored
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    initUi();
  }
  
  protected void initUi() {
    setCaption(i18nManager.getMessage(Messages.PEOPLE_INVOLVE_POPUP_CAPTION));
    setModal(true);
    addStyleName(Reindeer.WINDOW_LIGHT);
    center();
    
    windowLayout = (VerticalLayout) getContent();
    windowLayout.setSpacing(true);
    
    setWidth(700, UNITS_PIXELS);
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
    addComponent(searchField);
    
    // Logic to change table according to input
    searchField.addListener(new TextChangeListener() {
      public void textChange(TextChangeEvent event) {
        if (event.getText().length() >= 2) {
          matchingUsersTable.removeAllItems();
          List<UserCache.UserDetails> results = userCache.findMatchingUsers(event.getText());
          for (UserCache.UserDetails userDetails : results) {
            Item item = matchingUsersTable.addItem(userDetails.getUserId());
            item.getItemProperty("userName").setValue(userDetails.getFullName());
          }
        }
      }
    });
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
    // Panel containing table
    
    // table
   matchingUsersTable = new Table();
   matchingUsersTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
   matchingUsersTable.setSelectable(true);
   matchingUsersTable.setEditable(false);
   matchingUsersTable.setImmediate(true);
   matchingUsersTable.setMultiSelect(true);
   matchingUsersTable.setNullSelectionAllowed(false);
   matchingUsersTable.setSortDisabled(true);
   
   matchingUsersTable.setWidth(300, UNITS_PIXELS);
   matchingUsersTable.setHeight(200, UNITS_PIXELS);
   userSelectionLayout.addComponent(matchingUsersTable);
   
   matchingUsersTable.addContainerProperty("userName", String.class, null);
  }
  
  protected void initSelectUserButton() {
    selectUserButton = new Button(">");
    userSelectionLayout.addComponent(selectUserButton);
    userSelectionLayout.setComponentAlignment(selectUserButton, Alignment.MIDDLE_CENTER);
  }
  
  protected void initSelectedUsersTable() {
    Panel panel = new Panel();
    panel.setWidth(300, UNITS_PIXELS);
    panel.setHeight(200, UNITS_PIXELS);
    userSelectionLayout.addComponent(panel);
  }
  
  protected void initDoneButton() {
    doneButton = new Button("Done");
    addComponent(doneButton);
    windowLayout.setComponentAlignment(doneButton, Alignment.MIDDLE_RIGHT);
  }

}
