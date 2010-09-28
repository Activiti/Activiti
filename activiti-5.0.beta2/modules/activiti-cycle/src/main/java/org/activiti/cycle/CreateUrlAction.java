package org.activiti.cycle;

import java.net.URL;

/**
 * Base class for actions to open {@link URL}s in the GUI (e.g. download files,
 * open the modeler, ...). This action is used to create links which are added
 * to the {@link RepositoryArtifact} directly
 * 
 * @author ruecker
 */
public interface CreateUrlAction {

  public String getId();
  public URL getUrl(RepositoryConnector connector, RepositoryArtifact repositoryArtifact);
  
}
