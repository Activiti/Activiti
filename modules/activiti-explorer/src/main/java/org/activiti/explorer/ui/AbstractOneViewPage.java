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

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


/**
 * Superclass for one view pages
 * 
 * @author Tijs Rademakers
 */
public abstract class AbstractOneViewPage extends AbstractPage {

  private static final long serialVersionUID = 1L;
  
  /**
   * Override this method (and call super()) when you want to influence the UI.
   */
  protected void initUi() {
    addMainLayout();
    setSizeFull();
    addMenuBar();
  }

  /**
   * Subclasses are expected to provide their own menuBar.
   */
  protected void addMenuBar() {
    
    // Remove any old menu bar
    String activeEntry = null;
    if (toolBar != null) {
      activeEntry = toolBar.getCurrentEntryKey();
      grid.removeComponent(toolBar);
    }
    
    // Create menu bar
    ToolBar menuBar = createMenuBar();
    if (menuBar != null) {
      toolBar = createMenuBar();
      grid.addComponent(toolBar, 0, 0);
      
      if (activeEntry != null) {
        toolBar.setActiveEntry(activeEntry);
      }
    }
  }
  
  protected void addMainLayout() {
    grid = new GridLayout(1, 2);
    grid.setSizeFull();
    
    // Height division
    grid.setRowExpandRatio(1, 1.0f);
    
    setCompositionRoot(grid);
  }
  
  protected void setDetailComponent(Component detail) {
    if (grid.getComponent(0, 1) != null) {
      grid.removeComponent(0, 1);
    }
    if (detail != null) {
      grid.addComponent(detail, 0, 1);
    }
  }
  
  protected Component getDetailComponent() {
    return grid.getComponent(0, 1);
  }
}
