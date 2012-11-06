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

import org.activiti.editor.data.dao.ModelDao;
import org.activiti.editor.data.model.ModelData;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.listener.CopyModelClickListener;
import org.activiti.explorer.ui.process.listener.DeleteModelClickListener;
import org.activiti.explorer.ui.process.listener.DeployProcessDefinitionClickListener;
import org.activiti.explorer.ui.process.listener.EditModelClickListener;
import org.activiti.explorer.ui.process.listener.ExportModelClickListener;
import org.activiti.explorer.ui.process.listener.NewModelClickListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Panel showing model detail.
 * 
 * @author Tijs Rademakers
 */
public class EditorProcessDefinitionDetailPanel extends DetailPanel {
  
  private static final long serialVersionUID = 1L;
  
  // Members
  protected ModelData modelData;
  protected EditorProcessDefinitionPage processDefinitionPage;
  
  // Services
  protected I18nManager i18nManager;
  
  // UI
  protected VerticalLayout detailPanelLayout;
  protected HorizontalLayout detailContainer;
  protected HorizontalLayout actionsContainer;
  protected Label nameLabel;
  protected Button newModelButton;
  protected Button deployProcessDefinitionButton;
  protected Button exportModelButton;
  protected Button editModelButton;
  protected Button copyModelButton;
  protected Button deleteModelButton;
  
  protected FormPropertiesForm processDefinitionStartForm;
  protected EditorProcessDefinitionInfoComponent definitionInfoComponent;
  
  public EditorProcessDefinitionDetailPanel(long modelId, EditorProcessDefinitionPage processDefinitionPage) {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    this.processDefinitionPage = processDefinitionPage;
    this.modelData = new ModelDao().getModelById(modelId);

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
    
    deployProcessDefinitionButton = new Button(i18nManager.getMessage(Messages.PROCESS_DEPLOY));
    deployProcessDefinitionButton.addListener(new DeployProcessDefinitionClickListener(modelData));
    
    exportModelButton = new Button(i18nManager.getMessage(Messages.PROCESS_EXPORT));
    exportModelButton.addListener(new ExportModelClickListener(modelData));
    
    editModelButton = new Button(i18nManager.getMessage(Messages.PROCESS_EDIT));
    editModelButton.addListener(new EditModelClickListener(modelData.getObjectId()));
    
    copyModelButton = new Button(i18nManager.getMessage(Messages.PROCESS_COPY));
    copyModelButton.addListener(new CopyModelClickListener(modelData));
    
    deleteModelButton = new Button(i18nManager.getMessage(Messages.PROCESS_DELETE));
    deleteModelButton.addListener(new DeleteModelClickListener(modelData));
    
    // Clear toolbar and add 'start' button
    processDefinitionPage.getToolBar().removeAllButtons();
    processDefinitionPage.getToolBar().addButton(newModelButton);
    processDefinitionPage.getToolBar().addButton(deployProcessDefinitionButton);
    processDefinitionPage.getToolBar().addButton(exportModelButton);
    processDefinitionPage.getToolBar().addButton(editModelButton);
    processDefinitionPage.getToolBar().addButton(copyModelButton);
    processDefinitionPage.getToolBar().addButton(deleteModelButton);
  }
  

  public void initProcessDefinitionInfo() {
    if(definitionInfoComponent == null) {
      definitionInfoComponent = new EditorProcessDefinitionInfoComponent(modelData);
    }
    
    if (deployProcessDefinitionButton != null) {
      deployProcessDefinitionButton.setEnabled(true);
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
    String versionString = i18nManager.getMessage(Messages.PROCESS_VERSION, modelData.getRevision());
    Label versionLabel = new Label(versionString);
    versionLabel.addStyleName(ExplorerLayout.STYLE_PROCESS_HEADER_VERSION);
    propertiesLayout.addComponent(versionLabel);
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
}
