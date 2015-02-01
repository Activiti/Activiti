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

import java.util.HashMap;
import java.util.Map;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.themes.Reindeer;

/**
 * Generic component for a popup window that allows to display
 * multiple selection tabs on the left, and displays 
 * configured components matching those the selection on the right.
 * 
 * Note: For best visual results, the components on the right 
 * should be of fixed size, since the window width and heigh 
 * is calculated based on these components
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class TabbedSelectionWindow extends PopupWindow {

  private static final long serialVersionUID = 1L;

  protected I18nManager i18nManager;
  
  protected HorizontalLayout windowLayout;
  protected Table selectionTable;
  protected String currentSelection;
  protected Component currentComponent;
  protected Map<String, Component> components = new HashMap<String, Component>();
  protected Map<String, ClickListener> listeners = new HashMap<String, Button.ClickListener>();
  protected GridLayout selectedComponentLayout;
  protected Button okButton;
  
  public TabbedSelectionWindow(String title) {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    initWindow(title);
    initWindowLayout();
    initSelectionTable();
    initComponentLayout();
    initActions();
  }

  protected void initWindow(String title) {
    setCaption(title);
    center();
    setModal(true);
    addStyleName(Reindeer.WINDOW_LIGHT);
  }
  
  protected void initWindowLayout() {
    windowLayout = new HorizontalLayout();
    windowLayout.setSpacing(false);
    windowLayout.setMargin(true);
    windowLayout.setSizeFull();
    setContent(windowLayout);
  }
  
  protected void initComponentLayout() {
    selectedComponentLayout = new GridLayout(1,2);
    selectedComponentLayout.setSizeFull();
    selectedComponentLayout.setMargin(true);
    selectedComponentLayout.setSpacing(true);
    selectedComponentLayout.addStyleName(ExplorerLayout.STYLE_RELATED_CONTENT_CREATE_DETAIL);
    
    windowLayout.addComponent(selectedComponentLayout);
    windowLayout.setExpandRatio(selectedComponentLayout, 1.0f);
    
    selectedComponentLayout.setRowExpandRatio(0, 1.0f);
    selectedComponentLayout.setColumnExpandRatio(0, 1.0f);
  }

  protected void initActions() {
    okButton = new Button(i18nManager.getMessage(Messages.BUTTON_OK));
    selectedComponentLayout.addComponent(okButton, 0, 1);
    okButton.setEnabled(false);
    okButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        listeners.get(currentSelection).buttonClick(event);
        close();
      }
    });
    selectedComponentLayout.setComponentAlignment(okButton, Alignment.BOTTOM_RIGHT);
  }

  protected void initSelectionTable() {
    selectionTable = new Table();
    selectionTable.setSizeUndefined();
    selectionTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    selectionTable.setSelectable(true);
    selectionTable.setImmediate(true);
    selectionTable.setNullSelectionAllowed(false);
    selectionTable.setWidth(150, UNITS_PIXELS);
    selectionTable.setHeight(100, UNITS_PERCENTAGE);

    selectionTable.setCellStyleGenerator(new CellStyleGenerator() {
      private static final long serialVersionUID = 1L;
      public String getStyle(Object itemId, Object propertyId) {
        if("name".equals(propertyId)) {
          return ExplorerLayout.STYLE_RELATED_CONTENT_CREATE_LIST_LAST_COLUMN;
        }
        return null;
      }
    });

    selectionTable.addStyleName(ExplorerLayout.STYLE_RELATED_CONTENT_CREATE_LIST);

    selectionTable.addContainerProperty("type", Embedded.class, null);
    selectionTable.setColumnWidth("type", 22);
    selectionTable.addContainerProperty("name", String.class, null);

    // Listener to switch to the selected component
    selectionTable.addListener(new ValueChangeListener() {
      private static final long serialVersionUID = 1L;
      public void valueChange(ValueChangeEvent event) {
        String name = (String) event.getProperty().getValue();
        if (name != null) {
          currentSelection = name;
          currentComponent = components.get(name);
          selectedComponentLayout.removeComponent(selectedComponentLayout.getComponent(0, 0));
          if (currentComponent != null) {
            currentComponent.setSizeFull();
            selectedComponentLayout.addComponent(currentComponent, 0, 0);
            okButton.setEnabled(true);
          } else {
            okButton.setEnabled(false);
          }
        } 
      }
    });
    windowLayout.addComponent(selectionTable);
  }
  
  /**
   * @param icon The 16x16 icon that will be displayed on the left in the selection table.
   * @param name The name that will be shown in the selection table
   * @param component The component that is selected when the item in the selection table is clicked.
   * @param clickListener The listener that will be attached to the OK button displayed beneath
   *                      the component.
   */
  public void addSelectionItem(Embedded icon, String name, Component component, ClickListener clickListener) {
    Item item = selectionTable.addItem(name);
    item.getItemProperty("type").setValue(icon);
    item.getItemProperty("name").setValue(name);
    components.put(name, component);
    listeners.put(name, clickListener);
  }
  
}
