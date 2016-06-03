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

import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.form.FormPropertiesEventListener;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.form.FormPropertiesForm.FormPropertiesEvent;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ReportDetailPanel extends DetailPanel {

  private static final long serialVersionUID = 1L;
  
  protected ProcessDefinition processDefinition;
  protected AbstractPage parentPage;
  protected I18nManager i18nManager;
  
  protected VerticalLayout detailPanelLayout;
  protected HorizontalLayout detailContainer;
  protected FormPropertiesForm processDefinitionStartForm;
  
  protected Map<String, String> savedFormProperties;
  
  public ReportDetailPanel(String processDefinitionId, AbstractPage parentPage) {
    this.parentPage = parentPage;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    this.processDefinition = ProcessEngines.getDefaultProcessEngine().getRepositoryService()
            .createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    
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
    initActions();
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
    Label nameLabel = new Label(getReportDisplayName());
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    details.addComponent(nameLabel, 1, 0);

    // Properties
    HorizontalLayout propertiesLayout = new HorizontalLayout();
    propertiesLayout.setSpacing(true);
    details.addComponent(propertiesLayout);
    
    // Version
    String versionString = i18nManager.getMessage(Messages.PROCESS_VERSION, processDefinition.getVersion());
    Label versionLabel = new Label(versionString);
    versionLabel.addStyleName(ExplorerLayout.STYLE_PROCESS_HEADER_VERSION);
    propertiesLayout.addComponent(versionLabel);
  }
  
  protected void initActions() {
    final Button saveButton = new Button(i18nManager.getMessage(Messages.BUTTON_SAVE));
    saveButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        SaveReportPopupWindow saveReportPopupWindow = new SaveReportPopupWindow();
        saveReportPopupWindow.setProcessDefinitionId(processDefinition.getId());
        saveReportPopupWindow.setOriginalFormProperties(savedFormProperties);
        saveReportPopupWindow.setComponentToDisableOnClose(saveButton);
        ExplorerApp.get().getViewManager().showPopupWindow(saveReportPopupWindow);
      }
      
    });
    
    // Clear toolbar and add 'start' button
    parentPage.getToolBar().removeAllButtons();
    parentPage.getToolBar().addButton(saveButton);
  }
  
  protected void initForm() {
    // Check if a start form is defined
    final ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    StartFormData startFormData = processEngine.getFormService().getStartFormData(processDefinition.getId());
    
    if(startFormData != null && ((startFormData.getFormProperties() != null && !startFormData.getFormProperties()
                                                                                             .isEmpty()) || startFormData.getFormKey() != null)) {
      processDefinitionStartForm = new FormPropertiesForm();
      detailContainer.addComponent(processDefinitionStartForm);
      
      processDefinitionStartForm.setFormProperties(startFormData.getFormProperties());

      processDefinitionStartForm.setSubmitButtonCaption(i18nManager.getMessage(Messages.REPORTING_GENERATE_REPORT));
      processDefinitionStartForm.hideCancelButton();
      processDefinitionStartForm.addListener(new FormPropertiesEventListener() {

        private static final long serialVersionUID = 1L;

        protected void handleFormSubmit(FormPropertiesEvent event) {

          // Report is generated by running a process and storing the dataset in the history tablkes
          savedFormProperties = event.getFormProperties();
          ProcessInstance processInstance = startProcessInstanceWithFormProperties(processDefinition.getId(), event.getFormProperties());
          generateReport(processInstance);
        
        }

        protected void handleFormCancel(FormPropertiesEvent event) {
          // Not needed, cancel button not shown in report panels
        }
      });

      
    } else {
      // Just start the process-instance since it has no form.
      ProcessInstance processInstance = startProcessInstance(processDefinition.getId());
      generateReport(processInstance);
    }
  }
  
  protected ProcessInstance startProcessInstanceWithFormProperties(String processDefinitonId, Map<String, String> formProperties) {
    return ProcessEngines.getDefaultProcessEngine().getFormService()
            .submitStartFormData(processDefinitonId, formProperties);
  }
  
  protected ProcessInstance startProcessInstance(String processDefinitionId) {
    return ProcessEngines.getDefaultProcessEngine().getRuntimeService().startProcessInstanceById(processDefinitionId);
  }
  
  protected void generateReport(ProcessInstance processInstance) {
    // Report dataset is stored as historical variable as json
    HistoricVariableInstance historicVariableInstance = ProcessEngines.getDefaultProcessEngine()
            .getHistoryService()
            .createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.getId())
            .variableName("reportData")
            .singleResult();
    
    // Generate chart
    byte[] reportData = (byte[]) historicVariableInstance.getValue();
    ChartComponent chart = ChartGenerator.generateChart(reportData);
    chart.setWidth(100, UNITS_PERCENTAGE);
    chart.setHeight(100, UNITS_PERCENTAGE);
    
    // Put chart on screen
    if (processDefinitionStartForm != null) {
      detailContainer.removeComponent(processDefinitionStartForm);
      processDefinitionStartForm = null;
    }
    detailContainer.addComponent(chart);
    
    // The historic process instance can now be removed from the system
    // Only when save is clicked, the report will be regenerated
    ProcessEngines.getDefaultProcessEngine().getHistoryService().deleteHistoricProcessInstance(processInstance.getId());
  }
  
  protected String getReportDisplayName() {
    if(processDefinition.getName() != null) {
      return processDefinition.getName();
    } else {
      return processDefinition.getKey();
    }
  }

}
