package org.activiti.cycle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Base class for all actions requiring parameters (see
 * {@link ParametrizedAction}), loading the required HTML form
 * 
 * @author bernd.ruecker@camunda.com
 */
public abstract class ParametrizedFreemakerTemplateAction extends ParametrizedAction {

  public ParametrizedFreemakerTemplateAction() {
  }

  public ParametrizedFreemakerTemplateAction(RepositoryArtifact artifact) {
    super(artifact);
  }
  
  /**
   * returns a default form name you may want to use (expecting a file with the
   * class name + ".html" in the same package as the class
   */
  public String getDefaultFormName() {
    return "/" + this.getClass().getName().replace(".", "/") + ".html";
  }

  public String getFormAsHtml() {
    try {
      String resourceName = getFormResourceName();
      if (resourceName == null) {
        return null;
      }
      InputStream is = this.getClass().getResourceAsStream(resourceName);
      if (is == null) {
        throw new RepositoryException("HTML form for action " + this.getClass() + " from template '" + getFormResourceName() + "' doesn't exist in classpath");
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
      is.close();

      return sb.toString();
    } catch (Exception ex) {
      throw new RepositoryException("Exception while creating HTML form for action " + this.getClass() + " from form template '" + getFormResourceName() + "'",
              ex);
    }
  }

  public abstract String getFormResourceName();
}
