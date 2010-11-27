package org.activiti.cycle.impl.connector.util;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

/**
 * Utility methods for connectors
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
public class ConnectorPathUtils {

  public static String buildId(String... constituents) {
    String result = "";
    for (int i = 0; i < constituents.length; i++) {
      String constituent = constituents[i];
      while (constituent.endsWith("//")) {
        constituent = constituent.substring(0, constituent.length() - 1);
      }
      while (constituent.startsWith("//")) {
        constituent = constituent.substring(1);
      }
      if (i == 0) {
        result = constituents[i];
        continue;
      }
      if (!result.endsWith("/") && !constituent.startsWith("/") && constituent.length() > 0) {
        result += "/";
      }

      result += constituent;

    }
    return result;
  }

  public static ArtifactType getMimeType(String path, RepositoryConnectorConfiguration configuration) {
    // try to find artifact type
    ArtifactType artifactType = null;
    String extension = path;

    // TODO: figure out a better way to do this
    // problem exists with extensions like .bpmn20.xml
    while (extension.contains(".")) {
      extension = extension.substring(extension.indexOf(".") + 1);
      try {
        // throws exception if it cannot find an artifact type.
        artifactType = configuration.getArtifactType(extension);
      } catch (Exception e) {
        // let the exception pass
      }
      if (artifactType != null) {
        break;
      }

    }
    if (artifactType == null) {
      return configuration.getDefaultArtifactType();
    }

    return artifactType;

  }

}
