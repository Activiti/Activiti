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

package org.activiti.explorer.ui.management.processinstance;

import org.activiti.explorer.ui.AbstractTablePage;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;


/**
 * @author Frederik Heremans
 */
public class AlfrescoProcessInstanceDetailPanel extends ProcessInstanceDetailPanel {

  private static final long serialVersionUID = 1L;
  
  public AlfrescoProcessInstanceDetailPanel(String processInstanceId, AbstractTablePage processInstancePage) {
    super(processInstanceId, processInstancePage);
  }

  @Override
  protected Component getTaskAssigneeComponent(String assignee) {
    // Just return a label
    return new Label(assignee);
  }

}
