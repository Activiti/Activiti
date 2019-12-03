package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */

public class DeleteJobCmd implements Command<Object>, Serializable {

  private static final Logger log = LoggerFactory.getLogger(DeleteJobCmd.class);
  private static final long serialVersionUID = 1L;

  protected String jobId;

  public DeleteJobCmd(String jobId) {
    this.jobId = jobId;
  }

  public Object execute(CommandContext commandContext) {
    JobEntity jobToDelete = getJobToDelete(commandContext);
    sendCancelEvent(jobToDelete);
    commandContext.getJobEntityManager().delete(jobToDelete);
    return null;
  }

  protected void sendCancelEvent(JobEntity jobToDelete) {
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, jobToDelete));
    }
  }

  protected JobEntity getJobToDelete(CommandContext commandContext) {
    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("jobId is null");
    }
    if (log.isDebugEnabled()) {
      log.debug("Deleting job {}", jobId);
    }

    JobEntity job = commandContext.getJobEntityManager().findById(jobId);
    if (job == null) {
      throw new ActivitiObjectNotFoundException("No job found with id '" + jobId + "'", Job.class);
    }

    // We need to check if the job was locked, ie acquired by the job acquisition thread
    // This happens if the the job was already acquired, but not yet executed.
    // In that case, we can't allow to delete the job.
    if (job.getLockOwner() != null) {
      throw new ActivitiException("Cannot delete job when the job is being executed. Try again later.");
    }
    return job;
  }

}
