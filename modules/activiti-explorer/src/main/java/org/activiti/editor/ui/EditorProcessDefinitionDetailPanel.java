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
package org.activiti.editor.ui;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.listener.EditModelClickListener;
import org.activiti.explorer.ui.process.listener.ImportModelClickListener;
import org.activiti.explorer.ui.process.listener.NewModelClickListener;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Panel showing model detail.
 * 
 * @author Tijs Rademakers
 */
public class EditorProcessDefinitionDetailPanel extends DetailPanel {
  
  private static final long serialVersionUID = 1L;
  protected static final Logger LOGGER = LoggerFactory.getLogger(EditorProcessDefinitionDetailPanel.class);
  
  // Members
  protected Model modelData;
  protected EditorProcessDefinitionPage processDefinitionPage;
  
  // Services
  protected I18nManager i18nManager;
  
  // UI
  protected VerticalLayout detailPanelLayout;
  protected HorizontalLayout detailContainer;
  protected HorizontalLayout actionsContainer;
  protected Label nameLabel;
  protected Button newModelButton;
  protected Button importModelButton;
  protected Button editModelButton;
  protected Label actionLabel;
  protected Select actionSelect;
  
  protected FormPropertiesForm processDefinitionStartForm;
  protected EditorProcessDefinitionInfoComponent definitionInfoComponent;
  
  protected RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  
  public EditorProcessDefinitionDetailPanel(String modelId, EditorProcessDefinitionPage processDefinitionPage) {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    this.processDefinitionPage = processDefinitionPage;
    this.modelData = repositoryService.getModel(modelId);

    initUi();
  }
  
  protected void initUi() {
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    detailPanelLayout = new VerticalLayout();
    detailPanelLayout.setWidth(100, UNITS_PERCENTAGE);
    detailPanelLayout.setMargin(true);
    setDetailContainer(detailPanelLayout);
    
    // All details about the process definition
    initHeader();
    
    detailContainer = new HorizontalLayout();
    detailContainer.addStyleName(Reindeer.PANEL_LIGHT);
    detailPanelLayout.addComponent(detailContainer);
    detailContainer.setSizeFull();
    
    initActions();
    initProcessDefinitionInfo();
  }
  
  protected void initActions() {
    newModelButton = new Button(i18nManager.getMessage(Messages.PROCESS_NEW));
    newModelButton.addListener(new NewModelClickListener());
    
    importModelButton = new Button(i18nManager.getMessage(Messages.PROCESS_IMPORT));
    importModelButton.addListener(new ImportModelClickListener());
    
    editModelButton = new Button(i18nManager.getMessage(Messages.PROCESS_EDIT));
    editModelButton.addListener(new EditModelClickListener(modelData.getId()));
    
    actionLabel = new Label(i18nManager.getMessage(Messages.MODEL_ACTION));
    actionLabel.setSizeUndefined();
    
    actionSelect = new Select();
    actionSelect.addItem(i18nManager.getMessage(Messages.PROCESS_COPY));
    actionSelect.addItem(i18nManager.getMessage(Messages.PROCESS_DELETE));
    actionSelect.addItem(i18nManager.getMessage(Messages.PROCESS_DEPLOY));
    actionSelect.addItem(i18nManager.getMessage(Messages.PROCESS_EXPORT));
    
    actionSelect.setWidth("100px");
    actionSelect.setFilteringMode(Filtering.FILTERINGMODE_OFF);
    actionSelect.setImmediate(true);
    actionSelect.addListener(new ValueChangeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        if (i18nManager.getMessage(Messages.PROCESS_COPY).equals(event.getProperty().getValue())) {
          ExplorerApp.get().getViewManager().showPopupWindow(new CopyModelPopupWindow(modelData));
        } else if (i18nManager.getMessage(Messages.PROCESS_DELETE).equals(event.getProperty().getValue())) {
          ExplorerApp.get().getViewManager().showPopupWindow(new DeleteModelPopupWindow(modelData));
        } else if (i18nManager.getMessage(Messages.PROCESS_DEPLOY).equals(event.getProperty().getValue())) {
          deployModel();
        } else if (i18nManager.getMessage(Messages.PROCESS_EXPORT).equals(event.getProperty().getValue())) {
          exportModel();
        }
      }
    });
    
    // Clear toolbar and add 'start' button
    processDefinitionPage.getToolBar().removeAllButtons();
    processDefinitionPage.getToolBar().removeAllAdditionalComponents();
    processDefinitionPage.getToolBar().addButton(newModelButton);
    processDefinitionPage.getToolBar().addButton(importModelButton);
    processDefinitionPage.getToolBar().addButton(editModelButton);
    processDefinitionPage.getToolBar().addAdditionalComponent(actionLabel);
    processDefinitionPage.getToolBar().setComponentAlignment(actionLabel, Alignment.MIDDLE_LEFT);
    processDefinitionPage.getToolBar().addAdditionalComponent(actionSelect);
    processDefinitionPage.getToolBar().setComponentAlignment(actionSelect, Alignment.MIDDLE_RIGHT);
  }
  

  public void initProcessDefinitionInfo() {
    if(definitionInfoComponent == null) {
      definitionInfoComponent = new EditorProcessDefinitionInfoComponent(modelData);
    }
    
    detailContainer.removeAllComponents();
    detailContainer.addComponent(definitionInfoComponent);
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
    Embedded image = new Embedded(null, Images.PROCESS_50);
    details.addComponent(image, 0, 0, 0, 1);
    
    // Name
    Label nameLabel = new Label(modelData.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    details.addComponent(nameLabel, 1, 0);

    // Properties
    HorizontalLayout propertiesLayout = new HorizontalLayout();
    propertiesLayout.setSpacing(true);
    details.addComponent(propertiesLayout);
    
    // Version
    String versionString = i18nManager.getMessage(Messages.PROCESS_VERSION, modelData.getVersion());
    Label versionLabel = new Label(versionString);
    versionLabel.addStyleName(ExplorerLayout.STYLE_PROCESS_HEADER_VERSION);
    propertiesLayout.addComponent(versionLabel);
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
  
  protected void exportModel() {
    final FileResource stream = new FileResource(new File(""), ExplorerApp.get()) {
      
      private static final long serialVersionUID = 1L;

        @Override
        public DownloadStream getStream() {
          DownloadStream ds = null;
          try {
            
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);
            
            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
            String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
            ds = new DownloadStream(in, "application/xml", filename);
            // Need a file download POPUP
            ds.setParameter("Content-Disposition", "attachment; filename=" + filename);
          } catch(Exception e) {
            LOGGER.error("failed to export model to BPMN XML", e);
            ExplorerApp.get().getNotificationManager().showErrorNotification(Messages.PROCESS_TOXML_FAILED, e);
          }
          return ds;
        }
    };
    stream.setCacheTime(0);
    ExplorerApp.get().getMainWindow().open(stream);
  }
  
  protected void deployModel() {
    try {
      ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
      BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
      byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
      
      Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
          .addString(modelData.getName() + ".bpmn20.xml", new String(bpmnBytes)).deploy();
      
      ExplorerApp.get().getViewManager().showDeploymentPage(deployment.getId());
      
    } catch(Exception e) {
      ExplorerApp.get().getNotificationManager().showErrorNotification(Messages.PROCESS_TOXML_FAILED, e);
    }
  }
}
