package org.activiti.cycle.impl.connector.util;

import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.mimetype.UnknownMimeType;
import org.activiti.cycle.impl.repositoryartifacttype.BasicRepositoryArtifactType;
import org.activiti.cycle.impl.repositoryartifacttype.RepositoryArtifactTypes;

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

  public static RepositoryArtifactType getRepositoryArtifactType(String path) {
    // try to find artifact type
    RepositoryArtifactType artifactType = null;
    String extension = path;
    RepositoryArtifactTypes artifactTypes = CycleApplicationContext.get(RepositoryArtifactTypes.class);

    while (extension.contains(".")) {
      extension = extension.substring(extension.indexOf(".") + 1);
      artifactType = artifactTypes.getTypeForFilename(extension);

      // if we find something, keep it.
      if (artifactType != null && !artifactType.getMimeType().equals(new UnknownMimeType())) {
        break;
      }

    }
    if(artifactType == null) {
      return new BasicRepositoryArtifactType(CycleApplicationContext.get(UnknownMimeType.class));
    }

    return artifactType;

  }
}
