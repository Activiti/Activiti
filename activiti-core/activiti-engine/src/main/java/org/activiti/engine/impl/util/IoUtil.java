/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.activiti.engine.ActivitiException;

/**



 */
public class IoUtil {

  public static byte[] readInputStream(InputStream inputStream, String inputStreamName) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[16 * 1024];
    try {
      int bytesRead = inputStream.read(buffer);
      while (bytesRead != -1) {
        outputStream.write(buffer, 0, bytesRead);
        bytesRead = inputStream.read(buffer);
      }
    } catch (Exception e) {
      throw new ActivitiException("couldn't read input stream " + inputStreamName, e);
    }
    return outputStream.toByteArray();
  }

  public static String readFileAsString(String filePath) {
    byte[] buffer = new byte[(int) getFile(filePath).length()];
    BufferedInputStream inputStream = null;
    try {
      inputStream = new BufferedInputStream(new FileInputStream(getFile(filePath)));
      inputStream.read(buffer);
    } catch (Exception e) {
      throw new ActivitiException("Couldn't read file " + filePath + ": " + e.getMessage());
    } finally {
      IoUtil.closeSilently(inputStream);
    }
    return new String(buffer);
  }

  public static File getFile(String filePath) {
    URL url = IoUtil.class.getClassLoader().getResource(filePath);
    try {
      return new File(url.toURI());
    } catch (Exception e) {
      throw new ActivitiException("Couldn't get file " + filePath + ": " + e.getMessage());
    }
  }

  public static void writeStringToFile(String content, String filePath) {
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(getFile(filePath)));
      outputStream.write(content.getBytes());
      outputStream.flush();
    } catch (Exception e) {
      throw new ActivitiException("Couldn't write file " + filePath, e);
    } finally {
      IoUtil.closeSilently(outputStream);
    }
  }

  /**
   * Closes the given stream. The same as calling {@link InputStream#close()}, but errors while closing are silently ignored.
   */
  public static void closeSilently(InputStream inputStream) {
    try {
      if (inputStream != null) {
        inputStream.close();
      }
    } catch (IOException ignore) {
      // Exception is silently ignored
    }
  }

  /**
   * Closes the given stream. The same as calling {@link OutputStream#close()} , but errors while closing are silently ignored.
   */
  public static void closeSilently(OutputStream outputStream) {
    try {
      if (outputStream != null) {
        outputStream.close();
      }
    } catch (IOException ignore) {
      // Exception is silently ignored
    }
  }
}
