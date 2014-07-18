package org.activiti.engine.impl.cmd;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;

/**
 * Command that dispatches a JOB_CANCELLED event and deletes the job entity.
 */
public class CancelJobCmd extends DeleteJobCmd {
    
  private static final long serialVersionUID = 1L;

  public CancelJobCmd(String jobId) {
    super(jobId);
  }

  @Override
  public Object execute(CommandContext commandContext) {
    JobEntity jobToDelete = getJobToDelete(commandContext);

    sendCancelEvent(jobToDelete);

    jobToDelete.delete();
    return null;
  }

  private void sendCancelEvent(JobEntity jobToDelete) {
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
        ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, jobToDelete));
    }
  }

}
