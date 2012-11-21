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

package org.activiti.explorer.ui.process.listener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Tijs Rademakers
 */
public class ExportModelClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  protected static final Logger LOGGER = Logger.getLogger(ExportModelClickListener.class.getName());
  
  protected RepositoryService repositoryService;
  protected NotificationManager notificationManager;
  protected Model modelData;
  
  public ExportModelClickListener(Model model) {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.notificationManager = ExplorerApp.get().getNotificationManager(); 
    this.modelData = model;
  }

  public void buttonClick(ClickEvent event) {
    
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
            LOGGER.log(Level.SEVERE, "failed to export model to BPMN XML", e);
            notificationManager.showErrorNotification(Messages.PROCESS_TOXML_FAILED, e);
          }
          return ds;
        }
    };
    stream.setCacheTime(0);
    ExplorerApp.get().getMainWindow().open(stream);
  }
}
