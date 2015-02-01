package org.activiti.engine;

import org.activiti.engine.runtime.Job;

/**
 * This exception is thrown when you try to execute a job that is not found (may
 * be due to cancelActiviti="true" for instance)..
 * 
 * @author Prabhat Tripathi
 */
public class JobNotFoundException extends ActivitiObjectNotFoundException {

  private static final long serialVersionUID = 1L;

  /** the id of the job */
  private String jobId;

  public JobNotFoundException(String jobId) {
    super("No job found with id '" + jobId + "'.", Job.class);
    this.jobId = jobId;
  }

  public String getJobId() {
    return this.jobId;
  }

}
