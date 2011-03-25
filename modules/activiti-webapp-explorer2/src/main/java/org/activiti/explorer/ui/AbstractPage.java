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
package org.activiti.explorer.ui;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Superclass for all Explorer pages
 * 
 * @author Joram Barrez
 */
public abstract class AbstractPage extends CustomComponent {

  private static final long serialVersionUID = 1L;
  
  protected VerticalLayout pageLayout;
  protected Component menuBar;
  protected HorizontalSplitPanel splitPanel;
  protected Table table;
  

  // Overriding attach(), so we can construct the components first, before the UI is built,
  // that way, all member fields of subclasses are initialized properly
  @Override
  public void attach() {
   initUi();
  }
  
  /**
   * Override this method (and call super()) when you want to influence the UI.
   */
  protected void initUi() {
    setSizeFull();
    addPageLayout();
    addMenuBar();
    addMainSplitPanel();
    addList();
  }
  
  protected void addPageLayout() {
    // The main layout of this page is a vertical layout:
    // on top there is the dynamic task menu bar, on the bottom the rest
    pageLayout = new VerticalLayout();
    pageLayout.setSizeFull();
    setCompositionRoot(pageLayout);
  }
  
  /**
   * Subclasses are expected to provide their own menuBar.
   */
  protected void addMenuBar() {
    menuBar = createMenuBar();
    pageLayout.addComponent(menuBar);
  }
  
  protected abstract Component createMenuBar();
  
  protected void addMainSplitPanel() {
    // The actual content of the page is a HorizontalSplitPanel,
    // with on the left side the task list
    splitPanel = new HorizontalSplitPanel();
    splitPanel.addStyleName(Reindeer.SPLITPANEL_SMALL);
    splitPanel.setSizeFull();
    splitPanel.setSplitPosition(17, HorizontalSplitPanel.UNITS_PERCENTAGE);
    pageLayout.addComponent(splitPanel);
    pageLayout.setExpandRatio(splitPanel, 1.0f);
  }
  
  protected void addList() {
    table = createList();
    
    // Set non-editable, selectable and full-size
    table.setEditable(false);
    table.setImmediate(true);
    table.setSelectable(true);
    table.setNullSelectionAllowed(false);
    table.setSortDisabled(true);
    table.setSizeFull();
    
    splitPanel.setFirstComponent(table);
  }
  
  protected abstract Table createList();
  
  public void refreshList() {
    Integer pageIndex = (Integer) table.getCurrentPageFirstItemId();
    Integer selectedIndex = (Integer) table.getValue();
    table.removeAllItems();
    
    // Remove all items
    table.getContainerDataSource().removeAllItems();
    
    // Try to select the next one in the list
    Integer max = table.getContainerDataSource().size();
    if(pageIndex > max) {
      pageIndex = max -1;
    }
    if(selectedIndex > max) {
      selectedIndex = max -1;
    }
    table.setCurrentPageFirstItemIndex(pageIndex);
    selectListElement(selectedIndex);
  }
  
  public void selectListElement(int index) {
    if (table.getContainerDataSource().size() > index) {
      table.select(index);
      table.setCurrentPageFirstItemId(index);
    }
  }

}
