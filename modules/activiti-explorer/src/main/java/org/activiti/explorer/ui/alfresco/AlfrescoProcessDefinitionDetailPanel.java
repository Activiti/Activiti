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

import org.activiti.explorer.Messages;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.ProcessDefinitionDetailPanel;
import org.activiti.explorer.ui.process.ProcessDefinitionPage;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class AlfrescoProcessDefinitionDetailPanel extends ProcessDefinitionDetailPanel {

  private static final long serialVersionUID = 1L;

  public AlfrescoProcessDefinitionDetailPanel(String processDefinitionId, ProcessDefinitionPage processDefinitionPage) {
    super(processDefinitionId, processDefinitionPage);
  }
  
  protected void initActions() {
    // no actions (start process instance) needed for Alfresco
  }
  
  protected void initUi() {
    super.initUi(); // diagram etc.
    initProcessInstancesTable();
  }
  
  protected void initProcessInstancesTable() {
    ProcessInstanceTableLazyQuery query = new ProcessInstanceTableLazyQuery(processDefinition.getId());
    
    // Header
    Label instancesTitle = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCES) + " (" + query.size() + ")");
    instancesTitle.addStyleName(ExplorerLayout.STYLE_H3);
    instancesTitle.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    instancesTitle.addStyleName(ExplorerLayout.STYLE_NO_LINE);
    detailPanelLayout.addComponent(instancesTitle);

    if (query.size() > 0) {
      
      Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
      detailPanelLayout.addComponent(emptySpace);
      
      Table instancesTable = new Table();
      instancesTable.setWidth(400, UNITS_PIXELS);
      if (query.size() > 6) {
        instancesTable.setPageLength(6);
      } else {
        instancesTable.setPageLength(query.size());
      }
      
      LazyLoadingContainer container = new LazyLoadingContainer(query);
      instancesTable.setContainerDataSource(container);
      
      // container props
      instancesTable.addContainerProperty(AlfrescoProcessInstanceTableItem.PROPERTY_ID, String.class, null);
      instancesTable.addContainerProperty(AlfrescoProcessInstanceTableItem.PROPERTY_BUSINESSKEY, String.class, null);
      instancesTable.addContainerProperty(AlfrescoProcessInstanceTableItem.PROPERTY_ACTIONS, Component.class, null);
      
      // column alignment
      instancesTable.setColumnAlignment(AlfrescoProcessInstanceTableItem.PROPERTY_ACTIONS, Table.ALIGN_CENTER);
      
      // column header
      instancesTable.setColumnHeader(AlfrescoProcessInstanceTableItem.PROPERTY_ID, i18nManager.getMessage(Messages.PROCESS_INSTANCE_ID));
      instancesTable.setColumnHeader(AlfrescoProcessInstanceTableItem.PROPERTY_BUSINESSKEY, i18nManager.getMessage(Messages.PROCESS_INSTANCE_BUSINESSKEY));
      instancesTable.setColumnHeader(AlfrescoProcessInstanceTableItem.PROPERTY_ACTIONS, i18nManager.getMessage(Messages.PROCESS_INSTANCE_ACTIONS));
      
      instancesTable.setEditable(false);
      instancesTable.setSelectable(true);
      instancesTable.setNullSelectionAllowed(false);
      instancesTable.setSortDisabled(true);
      detailPanelLayout.addComponent(instancesTable);
      
    } else {
      Label noInstances = new Label(i18nManager.getMessage(Messages.PROCESS_NO_INSTANCES));
      detailPanelLayout.addComponent(noInstances);
    }
  }
  
}
