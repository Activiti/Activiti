package org.activiti.cycle.impl.connector.demo.action;

import java.net.MalformedURLException;
import java.net.URL;

import org.activiti.cycle.CreateUrlAction;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.AbstractArtifactActionImpl;

/**
 * 
 * 
 * @author ruecker
 */
public class OpenActivitiAction extends AbstractArtifactActionImpl implements CreateUrlAction {

  private static final long serialVersionUID = 1L;
  
  public OpenActivitiAction() {
  }

  public OpenActivitiAction(String id) {
    super(id);
  }

  public URL getUrl(RepositoryConnector connector, RepositoryArtifact repositoryArtifact) {
    try {
      return new URL("http://www.activiti.org/cycle.html");
    } catch (MalformedURLException e) {
      throw new RepositoryException("Activiti URL couldn't be created (huh?) in demo connector", e);
    }
  }
}
