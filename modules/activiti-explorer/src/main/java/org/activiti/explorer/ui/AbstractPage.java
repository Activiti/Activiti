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

import org.activiti.explorer.ui.custom.ToolBar;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Superclass for all Explorer pages
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public abstract class AbstractPage extends CustomComponent {

  private static final long serialVersionUID = 1L;
  
  protected ToolBar toolBar;
  protected GridLayout grid;
  protected AbstractSelect select;
  protected boolean showEvents;
  
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
    showEvents = getEventComponent() != null;
    
    addMainLayout();
    setSizeFull();
    addMenuBar();
    addSearch();
    addSelectComponent();
    if(showEvents) {
      addEventComponent();
    }
  }
  
  protected void addEventComponent() {
    grid.addComponent(getEventComponent(), 2, 0, 2, 2);
  }

  /**
   * Subclasses are expected to provide their own menuBar.
   */
  protected void addMenuBar() {
    toolBar = createMenuBar();
    grid.addComponent(toolBar, 0, 0 , 1, 0);
  }
  
  public ToolBar getToolBar() {
    return toolBar;
  }
  
  protected abstract ToolBar createMenuBar();
  
  protected void addMainLayout() {
    if(showEvents) {
      grid = new GridLayout(3, 3);
      grid.setColumnExpandRatio(0, .25f);
      grid.setColumnExpandRatio(1, .52f);
      grid.setColumnExpandRatio(2, .23f);
    } else {
      grid = new GridLayout(2, 3);

      grid.setColumnExpandRatio(0, .25f);
      grid.setColumnExpandRatio(1, .75f);
    }
    
    grid.addStyleName(Reindeer.SPLITPANEL_SMALL);
    grid.setSizeFull();
    
    // Height division
    grid.setRowExpandRatio(2, 1.0f);
    
    setCompositionRoot(grid);
  }
  
  protected void addSearch() {
    Component searchComponent = getSearchComponent();
    if(searchComponent != null) {
      grid.addComponent(searchComponent, 0, 1);
    }
  }
  
  protected void addSelectComponent() {
    AbstractSelect select = createSelectComponent();
    grid.addComponent(select, 0, 2);
  }

  /**
   * Returns an implementation of {@link AbstractSelect},
   * which will be displayed on the left side of the page,
   * allowing to select elements from eg. a list, tree, etc.
   */
  protected abstract AbstractSelect createSelectComponent();
  
  /**
   * Refreshes the elements of the list, and selects the next
   * one (useful when the selected element is deleted).
   */
  public abstract void refreshSelectNext();
  
  /**
   * Select a specific element from the selection component.
   */
  public abstract void selectElement(int index);
  
  protected void setDetailComponent(Component detail) {
    if(grid.getComponent(1, 1) != null) {
      grid.removeComponent(1, 1);
    }
    if(detail != null) {
      grid.addComponent(detail, 1, 1, 1, 2);
    }
  }
  
  protected Component getDetailComponent() {
    return grid.getComponent(1, 0);
  }
  
  /**
   * Override to get the search component to display above the table. Return null
   * when no search should be displayed.
   */
  public Component getSearchComponent() {
    return null;
  }
  
  /**
   * Get the component to display the events in. 
   * 
   * Return null by default: no event-component will be used,
   * in that case the main UI will be two columns instead of three.
   * 
   * Override in case the event component must be shown:
   * three columns will be used then. 
   */
  protected Component getEventComponent() {
    return null;
  }

}
