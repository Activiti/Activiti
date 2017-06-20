package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntity;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */

public class DeleteDeadLetterJobCmd implements Command<Object>, Serializable {

  private static final Logger log = LoggerFactory.getLogger(DeleteDeadLetterJobCmd.class);
  private static final long serialVersionUID = 1L;

  protected String timerJobId;

  public DeleteDeadLetterJobCmd(String timerJobId) {
    this.timerJobId = timerJobId;
  }

  public Object execute(CommandContext commandContext) {
    DeadLetterJobEntity jobToDelete = getJobToDelete(commandContext);
    
    sendCancelEvent(jobToDelete);

    commandContext.getDeadLetterJobEntityManager().delete(jobToDelete);
    return null;
  }

  protected void sendCancelEvent(DeadLetterJobEntity jobToDelete) {
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, jobToDelete));
    }
  }

  protected DeadLetterJobEntity getJobToDelete(CommandContext commandContext) {
    if (timerJobId == null) {
      throw new ActivitiIllegalArgumentException("jobId is null");
    }
    if (log.isDebugEnabled()) {
      log.debug("Deleting job {}", timerJobId);
    }

    DeadLetterJobEntity job = commandContext.getDeadLetterJobEntityManager().findById(timerJobId);
    if (job == null) {
      throw new ActivitiObjectNotFoundException("No dead letter job found with id '" + timerJobId + "'", Job.class);
    }

    return job;
  }

}
