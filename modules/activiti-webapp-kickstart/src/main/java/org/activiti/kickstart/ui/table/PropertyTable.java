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
package org.activiti.kickstart.ui.table;

import java.util.Arrays;

import org.activiti.kickstart.ui.listener.AddPropertyClickListener;
import org.activiti.kickstart.ui.listener.DeletePropertyClickListener;

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

  public PropertyTable() {
    setEditable(true);
    setColumnReorderingAllowed(true);
    setPageLength(size());

    addContainerProperty("property", String.class, null);
    addContainerProperty("type", ComboBox.class, null);
    addContainerProperty("required", CheckBox.class, null);
    addContainerProperty("actions", HorizontalLayout.class, null);

    setColumnHeader("property", "Property");
    setColumnHeader("type", "Type");
    setColumnHeader("required", "Required?");
    setColumnHeader("actions", "Actions");
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
    newItem.getItemProperty("property").setValue(propertyName == null ? "My Property" : propertyName);

    // type
    ComboBox typeComboBox = new ComboBox("types", Arrays.asList("text", "number", "date"));
    typeComboBox.setNullSelectionAllowed(false);
    if (propertyType == null) {
      typeComboBox.setValue(typeComboBox.getItemIds().iterator().next());
    } else {
      typeComboBox.setValue(propertyType);
    }
    newItem.getItemProperty("type").setValue(typeComboBox);

    // required
    CheckBox requiredCheckBox = new CheckBox();
    requiredCheckBox.setValue(required == null ? false : required);
    newItem.getItemProperty("required").setValue(requiredCheckBox);

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

    newItem.getItemProperty("actions").setValue(actionButtons);
  }

}
