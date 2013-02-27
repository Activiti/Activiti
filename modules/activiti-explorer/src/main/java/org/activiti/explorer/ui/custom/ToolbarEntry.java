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

import java.io.Serializable;

import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Entry to use in a tool bar. Shows label and optional count.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class ToolbarEntry extends CustomComponent {

  private static final long serialVersionUID = 1L;
  
  protected String title;
  protected Long count;
  protected boolean active;
  protected ToolbarCommand command;
  protected String name;
  
  protected Button titleButton;
  protected Button countButton;
  protected HorizontalLayout layout;

  
  public ToolbarEntry(String key, String title) {
    this.name = key;
    this.title = title;
    addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    layout = new HorizontalLayout();
    setCompositionRoot(layout);
    setSizeUndefined();
    initLabelComponent();
    initCountComponent();
  }
  
  /**
   * Sets the count to be displayed. When count is null, no
   * count-component will be shown.
   */
  public void setCount(Long count) {
    this.count = count;
    if(count != null) {
      countButton.setCaption(count + "");
      if(!countButton.isVisible()) {
        countButton.setVisible(true);
      }
    } else {
      countButton.setVisible(true);
    }
  }
  
  public Long getCount() {
    return count;
  }
  
  public void setActive(boolean active) {
    if(this.active != active) {
      this.active = active;
      if(active) {
        titleButton.addStyleName(ExplorerLayout.STYLE_ACTIVE);
        countButton.addStyleName(ExplorerLayout.STYLE_ACTIVE);
      } else {
        titleButton.removeStyleName(ExplorerLayout.STYLE_ACTIVE);
        countButton.removeStyleName(ExplorerLayout.STYLE_ACTIVE);
      }
    }
  }
  
  public void setCommand(ToolbarCommand command) {
    this.command = command;
  }

  protected void initLabelComponent() {
    titleButton = new Button(title);
    titleButton.addStyleName(Reindeer.BUTTON_LINK);
    layout.addComponent(titleButton);
    layout.setComponentAlignment(titleButton, Alignment.MIDDLE_LEFT);
    
    titleButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        if(command != null) {
          command.toolBarItemSelected();
        }
      }
    });
  }

  protected void initCountComponent() {
    countButton = new Button(count + "");
    countButton.addStyleName(Reindeer.BUTTON_LINK);
    countButton.addStyleName(ExplorerLayout.STYLE_TOOLBAR_COUNT);
    
    // Initially hidden
    countButton.setVisible(false);
    
    layout.addComponent(countButton);
    layout.setComponentAlignment(countButton, Alignment.MIDDLE_LEFT);
  }
  
  public interface ToolbarCommand extends Serializable {
    void toolBarItemSelected();
  }
}
