package org.activiti.cycle;

/**
 * Exception to indicate a requested node was not found
 * 
 * @author ruecker
 */
public class RepositoryNodeNotFoundException extends RepositoryException {

  private static final long serialVersionUID = 1L;

  public static String createNodeNotFoundMessage(String repositoryName, Class< ? extends RepositoryNode> artifactType, String artifactId) {
    return artifactType.getSimpleName() + " with id '" + artifactId + "' not found in repository '" + repositoryName + "'";
  }
  public static String createChildrenNotFoundMessage(String repositoryName, Class< ? extends RepositoryNode> artifactType, String artifactId) {
    return "Children for " + artifactType.getSimpleName() + " with id '" + artifactId + "' couldn't be loaded in repository '" + repositoryName + "'";
  }

  public RepositoryNodeNotFoundException(String repositoryName, Class< ? extends RepositoryNode> artifactType, String artifactId) {
    super(createNodeNotFoundMessage(repositoryName, artifactType, artifactId));
  }

  public RepositoryNodeNotFoundException(String repositoryName, Class< ? extends RepositoryNode> artifactType, String artifactId, Throwable cause) {
    super(createNodeNotFoundMessage(repositoryName, artifactType, artifactId), cause);
  }

  public RepositoryNodeNotFoundException(String msg) {
    super(msg);
  }

  public RepositoryNodeNotFoundException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
