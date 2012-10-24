package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;

/**
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */

public class DeleteJobCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(DeleteJobCmd.class.getName());

  protected String jobId;

  public DeleteJobCmd(String jobId) {
    this.jobId = jobId;
  }

  public Object execute(CommandContext commandContext) {
    if (jobId == null) {
      throw new ActivitiException("jobId is null");
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("Deleting job " + jobId);
    }

    JobEntity job = commandContext.getJobManager().findJobById(jobId);
    if (job == null) {
      throw new ActivitiException("No job found with id '" + jobId + "'");
    }
    
    // We need to check if the job was locked, ie acquired by the job acquisition thread
    // This happens if the the job was already acquired, but not yet executed.
    // In that case, we can't allow to delete the job.
    if (job.getLockOwner() != null || job.getLockExpirationTime() != null)
    {
      throw new ActivitiException("Cannot delete job when the job is being executed. Try again later.");
    }

    job.delete();
    return null;
  }

}
