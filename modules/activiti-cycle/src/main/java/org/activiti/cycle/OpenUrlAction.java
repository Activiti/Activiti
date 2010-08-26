package org.activiti.cycle;

import java.net.URL;

/**
 * Base class for actions to open {@link URL}s in the GUI (e.g. download files,
 * open the modeler, ...)
 * 
 * @author ruecker
 */
public abstract class OpenUrlAction extends ArtifactAction {

  public OpenUrlAction() {
  }

  public OpenUrlAction(RepositoryArtifact artifact) {
    super(artifact);
  }

  public abstract URL getUrl();

  // maybe a "openInNewPage()" Parameter
  
}
