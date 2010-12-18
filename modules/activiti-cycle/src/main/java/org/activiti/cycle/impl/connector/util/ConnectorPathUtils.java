package org.activiti.cycle.impl.connector.util;

import org.activiti.cycle.MimeType;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.mimetype.Mimetypes;
import org.activiti.cycle.impl.mimetype.UnknownMimeType;

/**
 * Utility methods for filesystem-based connectors
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

  public static MimeType getMimeType(String path) {
    MimeType mimeType = null;
    String extension = path;
    Mimetypes mimeTypes = CycleApplicationContext.get(Mimetypes.class);
    MimeType unknownMimeType = CycleApplicationContext.get(UnknownMimeType.class);

    while (extension.contains(".")) {
      extension = extension.substring(extension.indexOf(".") + 1);
      mimeType = mimeTypes.getTypeForFilename(extension);
      // if we find something, keep it.
      if (mimeType != null && !mimeType.equals(unknownMimeType)) {
        break;
      }
    }
    if (mimeType == null) {
      return unknownMimeType;
    }
    return mimeType;

  }
}
