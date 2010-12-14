package org.activiti.cycle;


/**
 * A content provider can create content, normaly nevessary for a
 * {@link ContentRepresentation} for a special {@link RepositoryArtifact}
 * 
 * @author bernd.ruecker@camunda.com
 */
public interface ContentProvider {

  public Content createContent(RepositoryConnector connector, RepositoryArtifact artifact);
  
}
