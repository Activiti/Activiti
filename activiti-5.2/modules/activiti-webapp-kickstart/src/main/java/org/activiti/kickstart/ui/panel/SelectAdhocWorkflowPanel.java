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
package org.activiti.kickstart.ui.panel;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.activiti.kickstart.dto.KickstartWorkflowInfo;
import org.activiti.kickstart.service.KickstartService;
import org.activiti.kickstart.service.ServiceLocator;
import org.activiti.kickstart.ui.ViewManager;
import org.activiti.kickstart.ui.listener.EditExistingKickstartWorkflowClickListener;
import org.activiti.kickstart.ui.popup.ProcessImagePopupWindow;

import com.vaadin.data.Item;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class SelectAdhocWorkflowPanel extends Panel {

  protected static final long serialVersionUID = 3103964043105524411L;

  protected static final String TITLE = "Select existing adhoc workflow";

  protected Label titleLabel;
  protected Table workflowTable;
//  protected Resource editImage;
//  protected Resource xmlImage;

  protected ViewManager viewManager;
  protected KickstartService adhocWorkflowService;

  public SelectAdhocWorkflowPanel(ViewManager viewManager) {
    this.viewManager = viewManager;
    this.adhocWorkflowService = ServiceLocator.getAdhocWorkflowService();
//    this.editImage = new ClassResource("images/edit.png", viewManager.getApplication());
//    this.xmlImage = new ClassResource("images/xml.png", viewManager.getApplication());

    setStyleName(Reindeer.PANEL_LIGHT);
    initTitle();
    initWorkflowTable();
    initWorkflowTableContents();
  }

  private void initTitle() {
    titleLabel = new Label(TITLE);
    titleLabel.setStyleName(Reindeer.LABEL_H1);
    addComponent(titleLabel);

    // add some empty space
    Label emptyLabel = new Label("");
    emptyLabel.setHeight("1.5em");
    addComponent(emptyLabel);
  }

  protected void initWorkflowTable() {
    workflowTable = new Table();

    workflowTable.setSelectable(true);
    workflowTable.setMultiSelect(false);

    workflowTable.addContainerProperty("name", Button.class, null);
    workflowTable.addContainerProperty("key", String.class, null);
    workflowTable.addContainerProperty("version", Integer.class, null);
    workflowTable.addContainerProperty("createTime", Date.class, null);
    workflowTable.addContainerProperty("nrOfRunningInstance", Integer.class, null);
    workflowTable.addContainerProperty("nrOfHistoricInstances", Integer.class, null);
    workflowTable.addContainerProperty("actions", HorizontalLayout.class, null);

    workflowTable.setColumnHeader("name", "Name");
    workflowTable.setColumnHeader("key", "Key");
    workflowTable.setColumnHeader("version", "Version");
    workflowTable.setColumnHeader("nrOfRunningInstance", "# running instances");
    workflowTable.setColumnHeader("nrOfHistoricInstances", "# historic instances");
    workflowTable.setColumnHeader("actions", "Actions");

    workflowTable.setColumnAlignment("version", Table.ALIGN_CENTER);
    workflowTable.setColumnAlignment("nrOfRunningInstance", Table.ALIGN_CENTER);
    workflowTable.setColumnAlignment("nrOfHistoricInstances", Table.ALIGN_CENTER);

    addComponent(workflowTable);
  }

  protected void initWorkflowTableContents() {
    List<KickstartWorkflowInfo> processDefinitions = adhocWorkflowService.findKickstartWorkflowInformation();
    for (final KickstartWorkflowInfo infoDto : processDefinitions) {
      Item workflowItem = workflowTable.getItem(workflowTable.addItem());
      Button nameButton = new Button(infoDto.getName());
      nameButton.setStyleName("link");
      nameButton.addListener(new Button.ClickListener() {

        private static final long serialVersionUID = 5671158538486627690L;

        public void buttonClick(ClickEvent event) {
          viewManager.showPopupWindow(new ProcessImagePopupWindow(viewManager, infoDto.getId()));
        }

      });
      workflowItem.getItemProperty("name").setValue(nameButton);
      workflowItem.getItemProperty("key").setValue(infoDto.getKey());
      workflowItem.getItemProperty("version").setValue(infoDto.getVersion());
      workflowItem.getItemProperty("createTime").setValue(infoDto.getCreateTime());
      workflowItem.getItemProperty("nrOfRunningInstance").setValue(infoDto.getNrOfRuntimeInstances());
      workflowItem.getItemProperty("nrOfHistoricInstances").setValue(infoDto.getNrOfHistoricInstances());

      HorizontalLayout actions = new HorizontalLayout();
      actions.setSpacing(true);

      Button editButton = new Button("edit");
      editButton.setStyleName("link");
//      editButton.setIcon(editImage);
      editButton.setData(infoDto.getId());
      editButton.addListener(new EditExistingKickstartWorkflowClickListener(viewManager, adhocWorkflowService));
      actions.addComponent(editButton);

      StreamResource.StreamSource streamSource = new StreamSource() {

        private static final long serialVersionUID = -8875067466181823014L;

        public InputStream getStream() {
          return ServiceLocator.getAdhocWorkflowService().getProcessBpmnXml(infoDto.getId());
        }
      };
      Link bpmnXmlLink = new Link("get xml", new StreamResource(streamSource, infoDto.getKey() + ".bpmn20.xml", viewManager.getApplication()));
//      bpmnXmlLink.setIcon(xmlImage);
      actions.addComponent(bpmnXmlLink);

      workflowItem.getItemProperty("actions").setValue(actions);
    }
    workflowTable.setPageLength(workflowTable.size());
  }

}
