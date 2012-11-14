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
package org.activiti.explorer.ui.alfresco;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
class AlfrescoProcessInstanceTableItem extends PropertysetItem implements Comparable<AlfrescoProcessInstanceTableItem> {

  private static final long serialVersionUID = 1L;
  
  public static final String PROPERTY_ID = "id";
  public static final String PROPERTY_BUSINESSKEY = "businessKey";
  public static final String PROPERTY_ACTIONS = "actions";
  
  public AlfrescoProcessInstanceTableItem(final ProcessInstance processInstance) {
    addItemProperty(PROPERTY_ID, new ObjectProperty<String>(processInstance.getId(), String.class));
    
    if (processInstance.getBusinessKey() != null) {
      addItemProperty(PROPERTY_BUSINESSKEY, new ObjectProperty<String>(processInstance.getBusinessKey(), String.class));
    }

    Button viewProcessInstanceButton = new Button(ExplorerApp.get().getI18nManager().getMessage(Messages.PROCESS_ACTION_VIEW));
    viewProcessInstanceButton.addStyleName(Reindeer.BUTTON_LINK);
    viewProcessInstanceButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        ExplorerApp.get().getViewManager().showProcessInstancePage(processInstance.getId());
      }
    });
    
    viewProcessInstanceButton.setIcon(Images.MAGNIFIER_16);
    addItemProperty(PROPERTY_ACTIONS, new ObjectProperty<Component>(viewProcessInstanceButton, Component.class));
  }

  public int compareTo(AlfrescoProcessInstanceTableItem other) {
    // process instances are ordered by id
    String id = (String) getItemProperty("id").getValue();
    String otherId = (String) other.getItemProperty("id").getValue();
    return id.compareTo(otherId);
  }
}