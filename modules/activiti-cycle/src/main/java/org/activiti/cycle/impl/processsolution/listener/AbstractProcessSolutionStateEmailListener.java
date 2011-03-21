package org.activiti.cycle.impl.processsolution.listener;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.event.CycleEventListener;
import org.activiti.cycle.impl.components.CycleEmailDispatcher;
import org.activiti.cycle.impl.processsolution.event.ProcessSolutionStateEvent;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.engine.identity.User;

/**
 * Abstract base class for {@link ProcessSolutionStateEvent}-listeners which
 * send a notification via email.
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class AbstractProcessSolutionStateEmailListener<T extends ProcessSolutionStateEvent> implements CycleEventListener<T> {

  protected Logger logger = Logger.getLogger(getClass().getName());

  /**
   * TODO: Make configurable (it is important to have a valid address here, otherwise the sending fails with a 550 error)
   * Use configuration property from activiti cfg.xml (mailServerDefaultFrom)
   */
  public static String fromEmailAddress = "activiti@camunda.com";

  protected CycleEmailDispatcher cycleEmailDispatcher = CycleComponentFactory.getCycleComponentInstance(CycleEmailDispatcher.class, CycleEmailDispatcher.class);

  public void onEvent(T event) {
    try {
      for (User user : getRecipients(event.getProcessSolution())) {
        cycleEmailDispatcher.sendEmail(fromEmailAddress, user.getEmail(), getSubject(event), getMessage(event));
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error while building email.", e);
      throw new RuntimeException("Error while building email.", e);
    }
  }

  protected abstract String getSubject(T event);
  protected abstract String getMessage(T event);

  protected List<User> getRecipients(ProcessSolution processSolution) {
    return CycleServiceFactory.getProcessSolutionService().getProcessSolutionCollaborators(processSolution.getId(), null);
  }

}
