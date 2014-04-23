package org.activiti.spring.autodeployment;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.core.io.Resource;

/**
 * Implementation of {@link AutoDeploymentStrategy} that performs a separate
 * deployment for each resource by name.
 * 
 * @author Tiese Barrell
 * 
 */
public class SingleResourceAutoDeploymentStrategy extends AbstractAutoDeploymentStrategy {

  /**
   * The deployment mode this strategy handles.
   */
  public static final String DEPLOYMENT_MODE = "single-resource";

  @Override
  protected String getDeploymentMode() {
    return DEPLOYMENT_MODE;
  }

  @Override
  public void deployResources(final String deploymentNameHint, final Resource[] resources, final RepositoryService repositoryService) {

    // Create a separate deployment for each resource using the resource name

    for (final Resource resource : resources) {

      final String resourceName = determineResourceName(resource);
      final DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().enableDuplicateFiltering().name(resourceName);

      try {
        if (resourceName.endsWith(".bar") || resourceName.endsWith(".zip") || resourceName.endsWith(".jar")) {
          deploymentBuilder.addZipInputStream(new ZipInputStream(resource.getInputStream()));
        } else {
          deploymentBuilder.addInputStream(resourceName, resource.getInputStream());
        }
      } catch (IOException e) {
        throw new ActivitiException("couldn't auto deploy resource '" + resource + "': " + e.getMessage(), e);
      }

      deploymentBuilder.deploy();
    }
  }

}
