package org.activiti.spring.autodeployment;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;

/**
 * Abstract base class for implementations of {@link AutoDeploymentStrategy}.
 * 
 * @author Tiese Barrell
 * 
 */
public abstract class AbstractAutoDeploymentStrategy implements AutoDeploymentStrategy {

  /**
   * Gets the deployment mode this strategy handles.
   * 
   * @return the name of the deployment mode
   */
  protected abstract String getDeploymentMode();

  @Override
  public boolean handlesMode(final String mode) {
    return StringUtils.equalsIgnoreCase(mode, getDeploymentMode());
  }

  /**
   * Determines the name to be used for the provided resource.
   * 
   * @param resource
   *          the resource to get the name for
   * @return the name of the resource
   */
  protected String determineResourceName(final Resource resource) {
    String resourceName = null;

    if (resource instanceof ContextResource) {
      resourceName = ((ContextResource) resource).getPathWithinContext();

    } else if (resource instanceof ByteArrayResource) {
      resourceName = resource.getDescription();

    } else {
      try {
        resourceName = resource.getFile().getAbsolutePath();
      } catch (IOException e) {
        resourceName = resource.getFilename();
      }
    }
    return resourceName;
  }

}
