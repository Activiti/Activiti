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
package org.activiti.explorer.ui.reports;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.util.time.HumanTime;

import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Frederik Heremans
 */
public class SavedReportDetailPanel extends DetailPanel {

  private static final long serialVersionUID = 1L;

  protected HistoricProcessInstance historicProcessInstance;
  protected I18nManager i18nManager;
  
  protected VerticalLayout detailPanelLayout;
  protected HorizontalLayout detailContainer;
  protected FormPropertiesForm processDefinitionStartForm;
  
  public SavedReportDetailPanel(String historicProcessInstance) {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    this.historicProcessInstance = ProcessEngines.getDefaultProcessEngine()
             .getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(historicProcessInstance).singleResult();

    initUi();
  }

  protected void initUi() {
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    detailPanelLayout = new VerticalLayout();
    detailPanelLayout.setWidth(100, UNITS_PERCENTAGE);
    detailPanelLayout.setMargin(true);
    setDetailContainer(detailPanelLayout);
    
    initHeader();
    
    detailContainer = new HorizontalLayout();
    detailContainer.addStyleName(Reindeer.PANEL_LIGHT);
    detailPanelLayout.addComponent(detailContainer);
    detailContainer.setSizeFull();
    
    initForm();
    
  }
  
  protected void initHeader() {
    GridLayout details = new GridLayout(2, 2);
    details.setWidth(100, UNITS_PERCENTAGE);
    details.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    details.setSpacing(true);
    details.setMargin(false, false, true, false);
    details.setColumnExpandRatio(1, 1.0f);
    detailPanelLayout.addComponent(details);
    
    // Image
    Embedded image = new Embedded(null, Images.REPORT_50);
    details.addComponent(image, 0, 0, 0, 1);
    
    // Name
    Label nameLabel = new Label(SavedReportListItem.getReportDisplayName(historicProcessInstance));
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    details.addComponent(nameLabel, 1, 0);

    // Properties
    HorizontalLayout propertiesLayout = new HorizontalLayout();
    propertiesLayout.setSpacing(true);
    details.addComponent(propertiesLayout);
    
    // Created Time
    String createLabel = i18nManager.getMessage(Messages.REPORTING_CREATE_TIME, new HumanTime(i18nManager).format(historicProcessInstance.getEndTime()));
    Label versionLabel = new Label(createLabel);
    versionLabel.addStyleName(ExplorerLayout.STYLE_PROCESS_HEADER_START_TIME);
    propertiesLayout.addComponent(versionLabel);
  }
  
  protected void initForm() {
      // Report dataset is stored as historical variable as json
      HistoricVariableInstance historicVariableInstance = ProcessEngines.getDefaultProcessEngine().getHistoryService()
              .createHistoricVariableInstanceQuery()
              .processInstanceId(historicProcessInstance.getId())
              .variableName("reportData")
              .singleResult();
      
      // Generate chart
      byte[] reportData = (byte[]) historicVariableInstance.getValue();
      ChartComponent chart = ChartGenerator.generateChart(reportData);
        chart.setWidth(100, UNITS_PERCENTAGE);
        chart.setHeight(100, UNITS_PERCENTAGE);
      
      // Put chart on screen
      detailContainer.addComponent(chart);
  }
  
}
