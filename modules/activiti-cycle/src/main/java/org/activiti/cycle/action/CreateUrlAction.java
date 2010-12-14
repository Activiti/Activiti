package org.activiti.cycle.action;

import java.net.URL;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;

/**
 * Base class for actions to open {@link URL}s in the GUI (e.g. download files,
 * open the modeler, ...). This action is used to create links which are added
 * to the {@link RepositoryArtifact} directly
 * 
 * @author ruecker
 */
public interface CreateUrlAction extends Action {

  public URL getUrl(RepositoryConnector connector, RepositoryArtifact repositoryArtifact);
  
}
