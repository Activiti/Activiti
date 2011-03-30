package org.activiti.cycle.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * @author daniel.meyer@camunda.com
 */
public class CycleTestUtils {

  public static String loadResourceAsString(String resourceName, Class clazz) {
    BufferedReader reader = null;
    try {
      InputStream is = clazz.getResourceAsStream(resourceName);
      reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      StringWriter resultWriter = new StringWriter();
      String line;
      while ((line = reader.readLine()) != null) {
        resultWriter.append(line + "\n");
      }
      reader.close();
      return resultWriter.toString();
    } catch (IOException e) {
      if (reader == null)
        return null;
      try {
        reader.close();
      } catch (IOException ex) {

      }
      return null;
    }
  }
  
  public static boolean deleteFileRec(File path) {
    if (path.exists() && path.isAbsolute()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteFileRec(files[i]);
        } else {
          files[i].delete();
        }
      }
    }
    return path.delete();
  }
}
