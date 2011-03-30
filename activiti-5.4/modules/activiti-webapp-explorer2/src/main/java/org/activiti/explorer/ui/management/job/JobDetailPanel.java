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
package org.activiti.explorer.ui.management.job;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.runtime.MessageEntity;
import org.activiti.engine.impl.runtime.TimerEntity;
import org.activiti.engine.runtime.Job;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Images;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.ExplorerLayout;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Frederik Heremans
 */
public class JobDetailPanel extends Panel {

  private static final long serialVersionUID = 1L;
  
  protected ManagementService managementService;
  protected Job job;
  protected JobPage parent;
  
  public JobDetailPanel(String jobId, JobPage parent) {
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
    this.job = managementService.createJobQuery().jobId(jobId).singleResult();
    this.parent = parent;
    
    getContent().setSizeFull();
    setSizeFull();
    
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    addJobName();
    addJobTime();
    addJobState();
  }
  
  protected void addJobName() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSpacing(true);
    addComponent(layout);
    
    // Name
    Label nameLabel = new Label(getJobLabel(job));
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    layout.addComponent(nameLabel);
    
    // Execute button
    Button executeButton = new Button(ExplorerApplication.getCurrent().getMessage(Messages.JOB_EXECUTE));
    executeButton.setIcon(Images.EXECUTE);
    executeButton.addStyleName(Reindeer.BUTTON_LINK);
    layout.addComponent(executeButton);
    layout.setComponentAlignment(executeButton, Alignment.MIDDLE_LEFT);
    executeButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 2916889209278497696L;
      
      public void buttonClick(ClickEvent event) {
        try {
          managementService.executeJob(job.getId());
        } catch(ActivitiException ae) {
          String errorMessage = ae.getMessage() + (ae.getCause() != null ? " (" + ae.getCause().getClass().getName() + ")" : "");
          ExplorerApplication.getCurrent().showErrorNotification("Error while executing job", errorMessage);
          
          // Refresh the current job
          parent.refreshCurrentJobDetails();
        }
      }
    });
  }
  
  protected String getJobLabel(Job theJob) {
    // Try figuring out the type
    if(theJob instanceof TimerEntity) {
      return ExplorerApplication.getCurrent().getMessage(Messages.JOB_TIMER) + " " + theJob.getId();
    } else if (theJob instanceof MessageEntity) {
      return ExplorerApplication.getCurrent().getMessage(Messages.JOB_MESSAGE) + " " + theJob.getId();
    } else {
      return ExplorerApplication.getCurrent().getMessage(Messages.JOB_DEFAULT_NAME) + " " + theJob.getId();
    }
  }

  protected void addJobTime() {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    addComponent(emptySpace);
    
    HorizontalLayout timeDetails = new HorizontalLayout();
    timeDetails.setSpacing(true);
    addComponent(timeDetails);
    
    Embedded clockImage = new Embedded(null, Images.CLOCK);
    timeDetails.addComponent(clockImage);
    
    String dueDateString = null;
    if(job.getDuedate() != null) {
      dueDateString = ExplorerApplication.getCurrent().getMessage(Messages.JOB_DUEDATE) + " " + new PrettyTime().format(job.getDuedate());
    } else {
      dueDateString = ExplorerApplication.getCurrent().getMessage(Messages.JOB_NO_DUEDATE);
    }
    Label timeLabel = new Label(dueDateString);
    timeDetails.addComponent(timeLabel);
    
    if(job.getDuedate() != null) {
      Label realCreateTime = new Label("(" + job.getDuedate() + ")");
      realCreateTime.addStyleName(Reindeer.LABEL_SMALL);
      realCreateTime.setSizeUndefined();
      timeDetails.addComponent(realCreateTime);
      timeDetails.setComponentAlignment(realCreateTime, Alignment.MIDDLE_CENTER);
    }
    
    timeDetails.setComponentAlignment(timeLabel, Alignment.MIDDLE_CENTER);
  }
  
  protected void addJobState() {
    Label processDefinitionHeader = new Label(ExplorerApplication.getCurrent().getMessage(Messages.JOB_HEADER_EXECUTION));
    processDefinitionHeader.addStyleName(ExplorerLayout.STYLE_JOB_DETAILS_HEADER);
    processDefinitionHeader.setWidth("95%");
    addComponent(processDefinitionHeader);
    
    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.setSizeFull();
    addComponent(layout);
    ((VerticalLayout) getContent()).setExpandRatio(layout, 1.0f);
    
    // Number of retries
    Label retrieslabel = new Label(getRetriesLabel(job));
    layout.addComponent(retrieslabel);
    
    // Exceptions
    if(job.getExceptionMessage() != null) {
      Label exceptionMessageLabel = new Label(ExplorerApplication.getCurrent().getMessage(Messages.JOB_ERROR) 
              + ": " + job.getExceptionMessage());
      exceptionMessageLabel.addStyleName(ExplorerLayout.STYLE_JOB_EXCEPTION_MESSAGE);
      layout.addComponent(exceptionMessageLabel);
      
      // Add Exception stacktrace
      String stack = managementService.getJobExceptionStacktrace(job.getId());
      
      Label stackTraceLabel = new Label(stack);
      stackTraceLabel.setContentMode(Label.CONTENT_PREFORMATTED);
      stackTraceLabel.addStyleName(ExplorerLayout.STYLE_JOB_EXCEPTION_TRACE);
      stackTraceLabel.setSizeFull();
      
      Panel stackPanel = new Panel();
      stackPanel.setWidth(100, UNITS_PERCENTAGE);
      stackPanel.setSizeFull();
      stackPanel.setScrollable(true);
      stackPanel.addComponent(stackTraceLabel);
      
      layout.addComponent(stackPanel);
      layout.setExpandRatio(stackPanel, 1.0f);
    } else {
      Label noException = new Label(ExplorerApplication.getCurrent().getMessage(Messages.JOB_NOT_EXECUTED));
      noException.addStyleName(Reindeer.LABEL_SMALL);
      layout.addComponent(noException);
      layout.setExpandRatio(noException, 1.0f);
    }
  }
  
  protected String getRetriesLabel(Job theJob) {
    String retriesString;
    if(theJob.getRetries() <= 0) {
      retriesString = ExplorerApplication.getCurrent().getMessage(Messages.JOB_NO_RETRIES);
    } else {
      retriesString = ExplorerApplication.getCurrent().getMessage(Messages.JOB_RETRIES) + ": " + theJob.getRetries();
    }
    return retriesString;
  }
}
