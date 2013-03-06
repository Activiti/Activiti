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

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Table;


/**
 * Superclass for all pages that have a table on the left side of the page.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public abstract class AbstractTablePage extends AbstractPage {

  private static final long serialVersionUID = 1L;
  
  protected Table table;
  
  protected AbstractSelect createSelectComponent() {
    table = createList();
    
    // Set non-editable, selectable and full-size
    table.setEditable(false);
    table.setImmediate(true);
    table.setSelectable(true);
    table.setNullSelectionAllowed(false);
    table.setSortDisabled(true);
    table.setSizeFull();
    return table;
  }
  
  /**
   * Concrete pages must implement this.
   * The table that is returned will be used for the 
   * list on the left side.
   */
  protected abstract Table createList();
  
  /**
   * Refresh the list on the left side and selects the next element in the table.
   * (useful when element of the list is deleted)
   */
  public void refreshSelectNext() {
    Integer pageIndex = (Integer) table.getCurrentPageFirstItemId();
    Integer selectedIndex = (Integer) table.getValue();
    table.removeAllItems();
    
    // Remove all items
    table.getContainerDataSource().removeAllItems();
    
    // Try to select the next one in the list
    Integer max = table.getContainerDataSource().size();
    if (max != 0) {
      if(pageIndex > max) {
        pageIndex = max -1;
      }
      if(selectedIndex > max) {
        selectedIndex = max -1;
      }
      table.setCurrentPageFirstItemIndex(pageIndex);
      selectElement(selectedIndex);
    } else {
      table.setCurrentPageFirstItemIndex(0);
    }
  }
  
  public void selectElement(int index) {
    if (table.getContainerDataSource().size() > index) {
      table.select(index);
      table.setCurrentPageFirstItemId(index);
    }
  }
  
}
