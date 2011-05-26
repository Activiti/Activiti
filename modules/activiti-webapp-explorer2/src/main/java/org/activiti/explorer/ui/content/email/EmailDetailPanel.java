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

package org.activiti.explorer.ui.content.email;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.impl.util.json.JSONTokener;
import org.activiti.engine.task.Attachment;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout.SpacingHandler;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.Panel;

/**
 * @author Frederik Heremans
 */
public class EmailDetailPanel extends Panel {

  private static final long serialVersionUID = 1L;

  protected I18nManager i18nManager;
  protected TaskService taskService;
  
  protected Label content;
  protected Attachment attachment;
  
  protected GridLayout gridLayout;


  public EmailDetailPanel(Attachment attachment) {
    setSizeFull();
    ((AbstractLayout) getContent()).setMargin(true);
    ((SpacingHandler) getContent()).setSpacing(true);
    addStyleName(Reindeer.PANEL_LIGHT);

    this.attachment = attachment;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    gridLayout = new GridLayout(2, 4);
    gridLayout.setSpacing(true);
    addComponent(gridLayout);
    
    InputStream contentStream = taskService.getAttachmentContent(attachment.getId());
    // TODO: Error handling
    JSONObject emailJson = new JSONObject(new JSONTokener(new InputStreamReader(contentStream)));

    String html = emailJson.getString(Constants.EMAIL_HTML_CONTENT);
    String subject = emailJson.getString(Constants.EMAIL_SUBJECT);
    String recipients = emailJson.getString(Constants.EMAIL_RECIPIENT);
    String sentDate = emailJson.getString(Constants.EMAIL_SENT_DATE);
    String receivedDate = emailJson.getString(Constants.EMAIL_RECEIVED_DATE);
    
    // Add subject
    addSimpleRow(Messages.EMAIL_SUBJECT, subject);
    addSimpleRow(Messages.EMAIL_RECIPIENTS, recipients);
    addSimpleRow(Messages.EMAIL_SENT_DATE, sentDate);
    addSimpleRow(Messages.EMAIL_RECEIVED_DATE, receivedDate);

    // Add HTML content
    addHtmlContent(html);
   
  }

  protected void addHtmlContent(String html) {
    Panel panel = new Panel();
    panel.setWidth(800, UNITS_PIXELS);
    panel.setHeight(300, UNITS_PIXELS);
    
    content = new Label(html, Label.CONTENT_XHTML);
    content.setHeight(100, UNITS_PERCENTAGE);
    
    panel.addComponent(content);
    addComponent(panel);
  }

  protected void addSimpleRow(String labelMessageKey, String content) {
    addLabel(labelMessageKey);
    
    Label subjectLabel = new Label(content);
    subjectLabel.setSizeUndefined();
    subjectLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    
    gridLayout.addComponent(subjectLabel);
    gridLayout.setComponentAlignment(subjectLabel, Alignment.MIDDLE_LEFT);
  }
  
  protected void addLabel(String messageKey) {
    Label theLabel  = new Label(i18nManager.getMessage(messageKey));
    theLabel.setSizeUndefined();
    gridLayout.addComponent(theLabel);
    
  }

}
