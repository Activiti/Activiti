package org.activiti.cycle.impl.connector.demo.action;

import java.net.MalformedURLException;
import java.net.URL;

import org.activiti.cycle.OpenUrlAction;
import org.activiti.cycle.RepositoryException;

/**
 * 
 * 
 * @author ruecker
 */
public class OpenActivitiAction extends OpenUrlAction {

  @Override
  public URL getUrl() {
    try {
      return new URL("http://www.activiti.org/cycle.html");
    } catch (MalformedURLException e) {
      throw new RepositoryException("Activiti URL couldn't be created (huh?) in demo connector", e);
    }
  }

  @Override
  public String getLabel() {
    return "open activiti homepage";
  }

}
