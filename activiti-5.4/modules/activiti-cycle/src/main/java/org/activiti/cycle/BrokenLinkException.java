package org.activiti.cycle;

/**
 * Exception for signaling that a Link is broken.
 * 
 * @author daniel.meyer@camunda.com
 */
public class BrokenLinkException extends RepositoryException {

  private static final long serialVersionUID = 1L;

  protected String fromArtifactId;

  protected String toArtifactId;

  public BrokenLinkException(String arg0) {
    super(arg0);
  }

  public BrokenLinkException(String arg0, Throwable e) {
    super(arg0, e);
  }

  public BrokenLinkException(String arg0, String fromArtifactId, String toArtifactId, Throwable e) {
    super(arg0, e);
    this.fromArtifactId = fromArtifactId;
    this.toArtifactId = toArtifactId;
  }

}
