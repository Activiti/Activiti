package org.activiti.cycle.impl.processsolution.listener;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;

/**
 * Holds the email addresses for sending notifications
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.NONE)
public class CycleNotificationEmailAddresses {

  private String fromEmailAddress;

  public String getFromEmailAddress() {    
    if (fromEmailAddress == null) {
      fromEmailAddress = CycleServiceFactory.getConfigurationService().getConfigurationValue("NotificationEmailAddresses", "defaultFromEmailAddress");      
    }
    if(fromEmailAddress == null) {
      fromEmailAddress = ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine()).getProcessEngineConfiguration().getMailServerDefaultFrom();
    }
    if(fromEmailAddress == null 
            || fromEmailAddress.equals("activiti@localhost") // activiti@localhost does not work for most mailservers
            ) {
      fromEmailAddress = "activiti@camunda.com";
    }
    return fromEmailAddress;
  }

}
