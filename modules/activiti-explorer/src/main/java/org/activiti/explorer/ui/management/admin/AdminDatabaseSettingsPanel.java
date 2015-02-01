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
package org.activiti.explorer.ui.management.admin;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Tijs Rademakers
 */
public class AdminDatabaseSettingsPanel extends DetailPanel {

  private static final long serialVersionUID = 1L;
  
  protected transient IdentityService identityService;
  protected I18nManager i18nManager;

  protected ProcessEngineConfigurationImpl engineConfiguration; 
  protected VerticalLayout panelLayout;
  protected HorizontalLayout detailLayout;
  protected GridLayout detailsGrid;
  
  public AdminDatabaseSettingsPanel() {
  	ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
  	engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    init();
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.PANEL_LIGHT);
    
    initPageTitle();
    initDatabaseSettingsDetails();
  }

  protected void initPageTitle() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidth(100, UNITS_PERCENTAGE);
    layout.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    layout.setSpacing(true);
    layout.setMargin(false, false, true, false);
    addDetailComponent(layout);
    
    Embedded databaseImage = new Embedded(null, Images.DATABASE_50);
    layout.addComponent(databaseImage);
    
    Label groupName = new Label(i18nManager.getMessage(Messages.DATABASE_TITLE));
    groupName.setSizeUndefined();
    groupName.addStyleName(Reindeer.LABEL_H2);
    layout.addComponent(groupName);
    layout.setComponentAlignment(groupName, Alignment.MIDDLE_LEFT);
    layout.setExpandRatio(groupName, 1.0f);
  }

  protected void initDatabaseSettingsDetails() {
    Label settingsHeader = new Label(i18nManager.getMessage(Messages.MGMT_MENU_DATABASE));
    settingsHeader.addStyleName(ExplorerLayout.STYLE_H3);
    settingsHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    
    addDetailComponent(settingsHeader);
    
    detailLayout = new HorizontalLayout();
    detailLayout.setSpacing(true);
    detailLayout.setMargin(true, false, true, false);
    addDetailComponent(detailLayout);
    
    initSettingsProperties();
  }
  
  protected void initSettingsProperties() {
    detailsGrid = new GridLayout(2, 3);
    detailsGrid.setSpacing(true);
    detailLayout.setMargin(true, true, true, false);
    detailLayout.addComponent(detailsGrid);
    
    // Database type
    Label typeLabel = new Label(i18nManager.getMessage(Messages.DATABASE_TYPE) + ": ");
    typeLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    detailsGrid.addComponent(typeLabel);
    Label typeValueLabel = new Label(engineConfiguration.getDatabaseType());
    detailsGrid.addComponent(typeValueLabel);
    
    // Database schema update
    Label schemaUpdateLabel = new Label(i18nManager.getMessage(Messages.DATABASE_UPDATE) + ": ");
    schemaUpdateLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    detailsGrid.addComponent(schemaUpdateLabel);
    Label schemaUpdateValueLabel = new Label(engineConfiguration.getDatabaseSchemaUpdate());
    detailsGrid.addComponent(schemaUpdateValueLabel);
    
    // Config type
    Label configTypeLabel = new Label(i18nManager.getMessage(Messages.DATABASE_CONFIG_TYPE) + ": ");
    configTypeLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    detailsGrid.addComponent(configTypeLabel);
    String databaseConfigType = getDatabaseType();
    Label configTypeValueLabel = new Label(databaseConfigType);
    detailsGrid.addComponent(configTypeValueLabel);
    
    if("JNDI".equals(databaseConfigType)) {
    	// JNDI
      Label jndiLabel = new Label(i18nManager.getMessage(Messages.DATABASE_JNDI) + ": ");
      jndiLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
      detailsGrid.addComponent(jndiLabel);
      Label jndiValueLabel = new Label(engineConfiguration.getDataSourceJndiName());
	    detailsGrid.addComponent(jndiValueLabel);
      
    } else if("Datasource".equals(databaseConfigType)){
    
    	// Datasource class
    	Label datasourceLabel = new Label(i18nManager.getMessage(Messages.DATABASE_DATASOURCE_CLASS) + ": ");
    	datasourceLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
	    detailsGrid.addComponent(datasourceLabel);
	    Label datasourceValueLabel = new Label(engineConfiguration.getDataSource().getClass().getName());
	    detailsGrid.addComponent(datasourceValueLabel);
    
    } else {
    	
    	// JDBC URL
	    Label jdbcURLLabel = new Label(i18nManager.getMessage(Messages.DATABASE_JDBC_URL) + ": ");
	    jdbcURLLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
	    detailsGrid.addComponent(jdbcURLLabel);
	    Label jdbcURLValueLabel = new Label(engineConfiguration.getJdbcUrl());
	    detailsGrid.addComponent(jdbcURLValueLabel);
    }
  }
  
  protected String getDatabaseType() {
  	String databaseType = null;
  	if(engineConfiguration.getDataSourceJndiName() != null) {
  		databaseType = "JNDI";
  	} else if(engineConfiguration.getDataSource() != null) {
  		databaseType = "Datasource";
  	} else {
  		databaseType = "JDBC config";
  	}
  	return databaseType;
  }

}
