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
package org.activiti.explorer.ui.process.simple.editor.table;

import java.util.Arrays;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.process.simple.editor.listener.AddPropertyClickListener;
import org.activiti.explorer.ui.process.simple.editor.listener.DeletePropertyClickListener;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;

/**
 * @author Joram Barrez
 */
public class PropertyTable extends Table {

  private static final long serialVersionUID = 6521446909987945815L;

  public static final String ID_PROPERTY_NAME = "property";
  public static final String ID_PROPERTY_TYPE = "type";
  public static final String ID_PROPERTY_REQUIRED = "required";
  public static final String ID_PROPERTY_ACTIONS = "actions";
  
  private static final String DEFAULT_PROPERTY_NAME = "My property";
  
  protected I18nManager i18nManager;
  
  public PropertyTable() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    setEditable(true);
    setColumnReorderingAllowed(true);
    setPageLength(size());

    addContainerProperty(ID_PROPERTY_NAME, String.class, null);
    addContainerProperty(ID_PROPERTY_TYPE, ComboBox.class, null);
    addContainerProperty(ID_PROPERTY_REQUIRED, CheckBox.class, null);
    addContainerProperty(ID_PROPERTY_ACTIONS, HorizontalLayout.class, null);

    setColumnHeader(ID_PROPERTY_NAME, i18nManager.getMessage(Messages.PROCESS_EDITOR_PROPERTY_NAME));
    setColumnHeader(ID_PROPERTY_TYPE,  i18nManager.getMessage(Messages.PROCESS_EDITOR_PROPERTY_TYPE));
    setColumnHeader(ID_PROPERTY_REQUIRED,  i18nManager.getMessage(Messages.PROCESS_EDITOR_PROPERTY_REQUIRED));
    setColumnHeader(ID_PROPERTY_ACTIONS,  i18nManager.getMessage(Messages.PROCESS_EDITOR_ACTIONS));
  }

  public void addPropertyRow() {
    addPropertyRow(null, null, null, null);
  }

  public void addPropertyRow(String propertyName, String propertyType, Boolean required) {
    addPropertyRow(null, propertyName, propertyType, required);
  }

  public void addPropertyRowAfter(Object itemId) {
    addPropertyRow(itemId, null, null, null);
  }

  protected void addPropertyRow(Object itemId, String propertyName, String propertyType, Boolean required) {
    Object newItemId = null;
    if (itemId == null) { // add at the end of list
      newItemId = addItem();
    } else {
      newItemId = addItemAfter(itemId);
    }
    Item newItem = getItem(newItemId);

    // name
    newItem.getItemProperty(ID_PROPERTY_NAME).setValue(propertyName == null ? DEFAULT_PROPERTY_NAME : propertyName);

    // type
    ComboBox typeComboBox = new ComboBox("", Arrays.asList("text", "number", "date"));
    typeComboBox.setNullSelectionAllowed(false);
    if (propertyType == null) {
      typeComboBox.setValue(typeComboBox.getItemIds().iterator().next());
    } else {
      typeComboBox.setValue(propertyType);
    }
    newItem.getItemProperty(ID_PROPERTY_TYPE).setValue(typeComboBox);

    // required
    CheckBox requiredCheckBox = new CheckBox();
    requiredCheckBox.setValue(required == null ? false : required);
    newItem.getItemProperty(ID_PROPERTY_REQUIRED).setValue(requiredCheckBox);

    // actions
    HorizontalLayout actionButtons = new HorizontalLayout();

    Button deleteRowButton = new Button("-");
    deleteRowButton.setData(newItemId);
    deleteRowButton.addListener(new DeletePropertyClickListener(this));
    actionButtons.addComponent(deleteRowButton);

    Button addRowButton = new Button("+");
    addRowButton.setData(newItemId);
    addRowButton.addListener(new AddPropertyClickListener(this));
    actionButtons.addComponent(addRowButton);

    newItem.getItemProperty(ID_PROPERTY_ACTIONS).setValue(actionButtons);
  }

}
