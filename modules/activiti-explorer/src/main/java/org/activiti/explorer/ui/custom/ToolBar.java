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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Container for holding {@link ToolbarEntry}s.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class ToolBar extends HorizontalLayout {
  
  private static final long serialVersionUID = 7957488256766569264L;
  
  protected Map<String, ToolbarEntry> entryMap;
  protected ToolbarEntry currentEntry;
  protected List<Button> actionButtons;

  public ToolBar() {
    entryMap = new HashMap<String, ToolbarEntry>();
    actionButtons = new ArrayList<Button>();
    
    setWidth("100%");
    setHeight(36, UNITS_PIXELS);
    addStyleName(ExplorerLayout.STYLE_TOOLBAR);
    setSpacing(true);
    setMargin(false, true, false, true);
    
    // Add label to fill excess space
    Label spacer = new Label();
    spacer.setContentMode(Label.CONTENT_XHTML);
    spacer.setValue("&nbsp;");
    addComponent(spacer);
    setExpandRatio(spacer, 1.0f);
  }
  
  /**
   * Add a new entry to the tool bar.
   */
  public ToolbarEntry addToolbarEntry(String key, String title, ToolbarCommand command) {
    if(entryMap.containsKey(key)) {
      throw new IllegalArgumentException("Toolbar already contains entry for key: " + key);
    }

    ToolbarEntry entry = new ToolbarEntry(key, title);
    if(command != null) {
      entry.setCommand(command);
    }
    
    entryMap.put(key, entry);
    addEntryComponent(entry);
    return entry;
  }
  
  /**
   * Add a new entry, which displays a pop-up-list when clicked. Items of that list can be added
   * on returned {@link ToolbarPopupEntry} instance.
   */
  public ToolbarPopupEntry addPopupEntry(String key, String title) {
    if(entryMap.containsKey(key)) {
      throw new IllegalArgumentException("Toolbar already contains entry for key: " + key);
    }
    
    ToolbarPopupEntry entry = new ToolbarPopupEntry(key, title);
    entryMap.put(key, entry);
    addEntryComponent(entry);
    return entry;
  }
  
  /**
   * Add a button to the toolbar. The buttons are rendered on the right of the 
   * toolbar.
   */
  public void addButton(Button button) {
    button.addStyleName(ExplorerLayout.STYLE_TOOLBAR_BUTTON);
    
    actionButtons.add(button);
    // Button is added after the spacer
    addComponent(button);
    setComponentAlignment(button, Alignment.MIDDLE_RIGHT);
  }
  
  public void removeAllButtons() {
    for(Button b : actionButtons) {
      removeComponent(b);
    }
  }
  
  public long getCount(String key) {
    ToolbarEntry toolbarEntry = entryMap.get(key);
    if(toolbarEntry == null) {
      throw new IllegalArgumentException("Toolbar doesn't contain an entry for key: " + key);
    }
    return toolbarEntry.getCount();
  }
 
  /**
   * Update the count field on the entry with the given key.
   */
  public void setCount(String key, Long count) {
    ToolbarEntry toolbarEntry = entryMap.get(key);
    if(toolbarEntry == null) {
      throw new IllegalArgumentException("Toolbar doesn't contain an entry for key: " + key);
    }
    toolbarEntry.setCount(count);
  }
  
  /**
   * Gets the entry for the given key. Returns null when entry is not present for the given key.
   */
  public ToolbarEntry getEntry(String key) {
    return entryMap.get(key);
  }
  
  /**
   * Set the entry active with the given key. Active entries will
   * have alternative style applied to them.
   */
  public synchronized void setActiveEntry(String key) {
    if(currentEntry != null) {
      currentEntry.setActive(false);
    }
    
    currentEntry = entryMap.get(key);
    if(currentEntry != null) {
      currentEntry.setActive(true);
    }
  }
  
  protected void addEntryComponent(ToolbarEntry entry) {
    addComponent(entry, getComponentCount() - 1 - actionButtons.size());
    setComponentAlignment(entry, Alignment.MIDDLE_LEFT);
  }
  
}
