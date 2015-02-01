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
import org.activiti.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.activiti.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.runtime.Job;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class JobDetailPanel extends DetailPanel {

  private static final long serialVersionUID = 1L;
  
  protected transient ManagementService managementService;
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  
  protected Job job;
  protected JobPage jobPage;
  
  public JobDetailPanel(String jobId, JobPage jobPage) {
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    
    this.job = managementService.createJobQuery().jobId(jobId).singleResult();
    this.jobPage = jobPage;
    
    init();
  }
  
  protected void init() {
    addHeader();
    addJobState();
    addActions();
  }
  
  protected void addActions() {
    Button deleteButton = new Button(i18nManager.getMessage(Messages.JOB_DELETE));
    deleteButton.setIcon(Images.DELETE);
    deleteButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        managementService.deleteJob(job.getId());
        
        notificationManager.showInformationNotification(Messages.JOB_DELETED);
        jobPage.refreshSelectNext();
      }
    });
    
    Button executeButton = new Button(i18nManager.getMessage(Messages.JOB_EXECUTE));
    executeButton.setIcon(Images.EXECUTE);
    executeButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        try {
          managementService.executeJob(job.getId());
          jobPage.refreshSelectNext();
        } catch (ActivitiException ae) {
          String errorMessage = ae.getMessage() + (ae.getCause() != null ? " (" + ae.getCause().getClass().getName() + ")" : "");
          notificationManager.showErrorNotification(Messages.JOB_ERROR, errorMessage);

          // Refresh the current job
          jobPage.refreshCurrentJobDetails();
        }
      }
    });
    
    jobPage.getToolBar().removeAllButtons();
    jobPage.getToolBar().addButton(executeButton);
    jobPage.getToolBar().addButton(deleteButton);
  }

  protected void addHeader() {
    GridLayout jobDetails = new GridLayout(3, 2);
    jobDetails.setWidth(100, UNITS_PERCENTAGE);
    jobDetails.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    jobDetails.setSpacing(true);
    jobDetails.setMargin(false, false, true, false);
    
    // Add image
    Embedded image = new Embedded(null, Images.JOB_50);
    jobDetails.addComponent(image, 0, 0, 0, 1);
    
    // Add job name
    Label nameLabel = new Label(getJobLabel(job));
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    jobDetails.addComponent(nameLabel, 1, 0, 2, 0);
    
    // Add due date
    PrettyTimeLabel dueDateLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.JOB_DUEDATE),
      job.getDuedate(), i18nManager.getMessage(Messages.JOB_NO_DUEDATE), false);
    dueDateLabel.addStyleName(ExplorerLayout.STYLE_JOB_HEADER_DUE_DATE);
    jobDetails.addComponent(dueDateLabel, 1, 1);
    
    jobDetails.setColumnExpandRatio(1, 1.0f);
    jobDetails.setColumnExpandRatio(2, 1.0f);
    
    addDetailComponent(jobDetails);
  }
  
  protected String getJobLabel(Job theJob) {
    // Try figuring out the type
    if(theJob instanceof TimerEntity) {
      return i18nManager.getMessage(Messages.JOB_TIMER, theJob.getId());
    } else if (theJob instanceof MessageEntity) {
      return i18nManager.getMessage(Messages.JOB_MESSAGE, theJob.getId());
    } else {
      return i18nManager.getMessage(Messages.JOB_DEFAULT_NAME, theJob.getId());
    }
  }

   
  protected void addJobState() {
    Label processDefinitionHeader = new Label(i18nManager.getMessage(Messages.JOB_HEADER_EXECUTION));
    processDefinitionHeader.addStyleName(ExplorerLayout.STYLE_H3);
    processDefinitionHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    processDefinitionHeader.setWidth(100, UNITS_PERCENTAGE);
    addComponent(processDefinitionHeader);
    
    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.setSizeFull();
    layout.setMargin(true,false, true, false);
    
    addDetailComponent(layout);
    setDetailExpandRatio(layout, 1.0f);
    
    // Exceptions
    if(job.getExceptionMessage() != null) {
      // Number of retries
      Label retrieslabel = new Label(getRetriesLabel(job));
      layout.addComponent(retrieslabel);
      
      // Exception
      Label exceptionMessageLabel = new Label(i18nManager.getMessage(Messages.JOB_ERROR) 
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
      
      if (job.getProcessDefinitionId() != null) {
        
        // This is a hack .. need to cleanify this in the engine
        JobEntity jobEntity = (JobEntity) job;
        if (jobEntity.getJobHandlerType().equals(TimerSuspendProcessDefinitionHandler.TYPE)) {
          addLinkToProcessDefinition(layout, i18nManager.getMessage(Messages.JOB_SUSPEND_PROCESSDEFINITION), false);
        } else if (jobEntity.getJobHandlerType().equals(TimerActivateProcessDefinitionHandler.TYPE)) {
          addLinkToProcessDefinition(layout, i18nManager.getMessage(Messages.JOB_ACTIVATE_PROCESSDEFINITION), true);
        } else {
          addNotYetExecutedLabel(layout);
        }
        
      } else {
      
        addNotYetExecutedLabel(layout);
        
      }
    }
  }

  protected void addLinkToProcessDefinition(final VerticalLayout verticalLayout, final String labelText, final boolean isSuspendedProcessDefinition) {
    HorizontalLayout  layout = new HorizontalLayout();
    verticalLayout.addComponent(layout);
    
    Label processDefinitionLabel = new Label(labelText);
    processDefinitionLabel.setSizeUndefined();
    layout.addComponent(processDefinitionLabel);
    
    layout.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    
    Button showProcessDefinitionLink = new Button(job.getProcessDefinitionId());
    showProcessDefinitionLink.addStyleName(Reindeer.BUTTON_LINK);
    showProcessDefinitionLink.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        if (isSuspendedProcessDefinition) {
          ExplorerApp.get().getViewManager().showSuspendedProcessDefinitionsPage(job.getProcessDefinitionId());
        } else {
          ExplorerApp.get().getViewManager().showActiveProcessDefinitionsPage(job.getProcessDefinitionId());
        }
      }
    });
    layout.addComponent(showProcessDefinitionLink);
  }

  private void addNotYetExecutedLabel(VerticalLayout layout) {
    Label noException = new Label(i18nManager.getMessage(Messages.JOB_NOT_EXECUTED));
    layout.addComponent(noException);
    layout.setExpandRatio(noException, 1.0f);
  }
  
  protected String getRetriesLabel(Job theJob) {
    String retriesString;
    if(theJob.getRetries() <= 0) {
      retriesString = i18nManager.getMessage(Messages.JOB_NO_RETRIES);
    } else {
      retriesString = i18nManager.getMessage(Messages.JOB_RETRIES, theJob.getRetries());
    }
    return retriesString;
  }
}
