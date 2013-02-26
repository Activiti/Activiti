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
import java.io.InputStream;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Tijs Rademakers
 */
public class EditorProcessDefinitionInfoComponent extends VerticalLayout {

  protected static final Logger LOGGER = LoggerFactory.getLogger(EditorProcessDefinitionInfoComponent.class);
  private static final long serialVersionUID = 1L;

  // Services
  protected transient RepositoryService repositoryService;
  protected I18nManager i18nManager;
  
  // Members
  protected Model modelData;
  
  // UI
  protected HorizontalLayout timeDetails;
  protected VerticalLayout processImageContainer;
  
  
  public EditorProcessDefinitionInfoComponent(Model model) {
    super();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.i18nManager = ExplorerApp.get().getI18nManager(); 
    
    this.modelData = model;
    
    addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    
    initImage();
  }
  
  protected void initImage() {
    processImageContainer = new VerticalLayout();
    
    Label processTitle = new Label(i18nManager.getMessage(Messages.PROCESS_HEADER_DIAGRAM));
    processTitle.addStyleName(ExplorerLayout.STYLE_H3);
    processImageContainer.addComponent(processTitle);
    
    StreamSource streamSource = null;
    final byte[] editorSourceExtra = repositoryService.getModelEditorSourceExtra(modelData.getId());
    if (editorSourceExtra != null) {
      streamSource = new StreamSource() {
        private static final long serialVersionUID = 1L;

        public InputStream getStream() {
          InputStream inStream = null;
          try {
            inStream = new ByteArrayInputStream(editorSourceExtra);
          } catch (Exception e) {
            LOGGER.warn("Error reading PNG in StreamSource", e);
          }
          return inStream;
        }
      };
    }

    if(streamSource != null) {
      Embedded embedded = new Embedded(null, new ImageStreamSource(streamSource, ExplorerApp.get()));
      embedded.setType(Embedded.TYPE_IMAGE);
      embedded.setSizeUndefined();
      
      Panel imagePanel = new Panel(); // using panel for scrollbars
      imagePanel.addStyleName(Reindeer.PANEL_LIGHT);
      imagePanel.setWidth(100, UNITS_PERCENTAGE);
      imagePanel.setHeight(700, UNITS_PIXELS);
      HorizontalLayout panelLayout = new HorizontalLayout();
      panelLayout.setSizeUndefined();
      imagePanel.setContent(panelLayout);
      imagePanel.addComponent(embedded);
      
      processImageContainer.addComponent(imagePanel);
    } else {
      Label noImageAvailable = new Label(i18nManager.getMessage(Messages.PROCESS_NO_DIAGRAM));
      processImageContainer.addComponent(noImageAvailable);
    }
    addComponent(processImageContainer);
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
  
}
