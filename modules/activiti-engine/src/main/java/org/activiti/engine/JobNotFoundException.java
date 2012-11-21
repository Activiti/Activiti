package org.activiti.engine;

/**
 * This exception is thrown when you try to execute a job that is not found (may
 * be due to cancelActiviti="true" for instance)..
 * 
 * @author Prabhat Tripathi
 */
public class JobNotFoundException extends ActivitiException {

  private static final long serialVersionUID = 1L;

  /** the id of the job */
  private String jobId;

  public JobNotFoundException(String jobId) {
    super("No job found with id '" + jobId + "'.");
    this.jobId = jobId;
  }

  public String getJobId() {
    return this.jobId;
  }

}
