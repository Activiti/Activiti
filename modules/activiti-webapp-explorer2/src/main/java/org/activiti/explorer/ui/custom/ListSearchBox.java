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

import org.activiti.explorer.ui.ExplorerLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextField;


/**
 * @author Frederik Heremans
 */
public class ListSearchBox extends HorizontalLayout {

  private static final long serialVersionUID = 1L;

  protected TextField searchField;
  
  public ListSearchBox() {
    setHeight(36, UNITS_PIXELS);
    setWidth(100, UNITS_PERCENTAGE);
    
    addStyleName(ExplorerLayout.STYLE_SEARCHBOX);
    setSpacing(true);
    
    addSpacer();
    initSearchBox();
    initSortMenu();
    
    addSpacer();
  }   

  protected void initSortMenu() {
    
    MenuBar menuBar = new MenuBar();
    menuBar.addStyleName(ExplorerLayout.STYLE_SEARCHBOX_SORTMENU);
    
    //TODO: Adding types of sorting manually and listener/events
    MenuItem rootItem = menuBar.addItem("Sort by", null);
    rootItem.addItem("Id", null);
    rootItem.addItem("Name", null);
    rootItem.addItem("Due date", null);
    rootItem.addItem("Creation date", null);
    
    addComponent(menuBar);
    setComponentAlignment(menuBar, Alignment.MIDDLE_RIGHT);
  }

  protected void addSpacer() {
    Label spacer = new Label("&nbsp");
    spacer.setContentMode(Label.CONTENT_XHTML);
    addComponent(spacer);
  }

  //TODO: Add handling of enter-event
  protected void initSearchBox() {
    // Csslayout is used to style inputtext as rounded
    CssLayout csslayout = new CssLayout();
    csslayout.setHeight(24, UNITS_PIXELS);
    csslayout.setWidth(100, UNITS_PERCENTAGE);
    
    searchField = new TextField();
    searchField.setWidth(100, UNITS_PERCENTAGE);
    searchField.addStyleName(ExplorerLayout.STYLE_SEARCHBOX);
    searchField.setVisible(true);
    
    csslayout.addComponent(searchField);
    
    addComponent(csslayout);
    setComponentAlignment(csslayout, Alignment.MIDDLE_LEFT);
    setExpandRatio(csslayout, 1.0f);
  }
  
  
}
