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

import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.IdentityService;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.ui.event.SubmitEvent;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Very simple popup window that allows to select groups.
 * Will probably be replaced in the future with something
 * more fancy (with search capatbilities etc)
 * 
 * @author Joram Barrez
 */
public class GroupSelectionPopupWindow extends PopupWindow {
  
  private static final long serialVersionUID = 1L;
  protected IdentityService identityService;
  protected I18nManager i18nManager;
  protected String userId;
  protected Table groupTable;

  public GroupSelectionPopupWindow(IdentityService identityService, String userId) {
    this.identityService = identityService;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.userId = userId;
    
    setCaption(i18nManager.getMessage(Messages.USER_SELECT_GROUPS_POPUP, userId));
    setModal(true);
    center();
    setWidth(500, UNITS_PIXELS);
    setHeight(400, UNITS_PIXELS);
    addStyleName(Reindeer.WINDOW_LIGHT);
    ((VerticalLayout) getContent()).setSpacing(true);
    
    initGroupTable();
    initSelectButton();
  }
  
  protected void initGroupTable() {
    groupTable = new Table();
    groupTable.setNullSelectionAllowed(false);
    groupTable.setSelectable(true);
    groupTable.setMultiSelect(true);
    groupTable.setSortDisabled(true);
    groupTable.setWidth(460, UNITS_PIXELS);
    groupTable.setHeight(275, UNITS_PIXELS);
    addComponent(groupTable);
    
    GroupSelectionQuery query = new GroupSelectionQuery(identityService, userId);
    LazyLoadingContainer container = new LazyLoadingContainer(query, 10);
    groupTable.setContainerDataSource(container);
    
    groupTable.addContainerProperty("id", String.class, null);
    groupTable.addContainerProperty("name", String.class, null);
    groupTable.addContainerProperty("type", String.class, null);
  }
  
  protected void initSelectButton() {
    final Button selectButton = new Button(i18nManager.getMessage(Messages.USER_SELECT_GROUPS));
    addComponent(selectButton);
    ((VerticalLayout) getContent()).setComponentAlignment(selectButton, Alignment.BOTTOM_RIGHT);
    
    selectButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        fireEvent(new SubmitEvent(selectButton, SubmitEvent.SUBMITTED));
        close();
      }
    });
  }
  
  @SuppressWarnings("unchecked")
  public Set<String> getSelectedGroupIds() {
    Set<String> groupIds = new HashSet<String>();
    for (Object itemId : (Set<Object>)groupTable.getValue()) {
      groupIds.add((String) groupTable.getItem(itemId).getItemProperty("id").getValue());
    }
    return groupIds;
  }

}
