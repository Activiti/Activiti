package org.activiti.cycle.impl.connector.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Stream utils for connector development
 * 
 * @author meyerd
 * 
 */
public class ConnectorStreamUtils {

  public static int DEFAULT_BUFFER_SIZE = 2024;

  /**
   * Copies content form is to os, using the specified buffer-size. (Closes the
   * os).
   * 
   * @param is
   *          the input stream to copy from
   * @param os
   *          the output stream to copy to
   * @param bufferSize
   * @throws IOException
   */
  public static void copyStreams(InputStream is, OutputStream os, int bufferSize) throws IOException {

    os = new BufferedOutputStream(os, bufferSize);
    is = new BufferedInputStream(is, bufferSize);
    // writing blocks of 2048 (is there a better number?)
    byte[] buffer = new byte[bufferSize];
    for (int c; (c = is.read(buffer)) > 0;) {
      os.write(buffer, 0, c);
    }
    os.flush();
    os.close();

  }

  /**
   * copies content form one stream to another, using a default buffer-size.
   * (Closes the os).
   * 
   * @param is
   *          the input stream to copy from
   * @param os
   *          the output stream to copy to
   * @throws IOException
   */
  public static void copyStreams(InputStream is, OutputStream os) throws IOException {
    copyStreams(is, os, DEFAULT_BUFFER_SIZE);
  }

}
