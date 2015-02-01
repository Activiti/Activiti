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
package org.activiti.explorer.ui.process.simple.editor.listener;

import org.activiti.explorer.ui.process.simple.editor.table.PropertyTable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * @author Joram Barrez
 */
public class AddPropertyClickListener implements Button.ClickListener {

  private static final long serialVersionUID = 7191125475609217512L;

  protected PropertyTable propertyTable;

  public AddPropertyClickListener(PropertyTable propertyTable) {
    this.propertyTable = propertyTable;
  }

  public void buttonClick(ClickEvent event) {
    Object itemId = event.getButton().getData();
    propertyTable.addPropertyRowAfter(itemId);
  }

}
