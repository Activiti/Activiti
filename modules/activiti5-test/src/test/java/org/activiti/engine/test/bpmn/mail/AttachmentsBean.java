/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.bpmn.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

import org.apache.commons.lang3.Validate;

public class AttachmentsBean implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final File RESOURCE_DIR = new File("src/test/resources/");
  private static final File PACKAGE_DIR = new File(RESOURCE_DIR, "org/activiti/engine/test/bpmn/mail/");

  public File getFile() {
    String fileName = "EmailServiceTaskTest.testTextMailWithFileAttachment.bpmn20.xml";
    File file = new File(PACKAGE_DIR, fileName);
    Validate.isTrue(file.exists(), "file <" + fileName + "> does not exist in dir "
      + PACKAGE_DIR.getAbsolutePath());
    return file;
  }

  public File[] getFiles() {
    File[] files = new File[3];
    String[] testNames = new String[] {
      "testTextMailWithFileAttachment",
      "testHtmlMailWithFileAttachment",
      "testTextMailWithFileAttachments",
    };
    for (int i = 0; i < files.length; i++) {
      String fileName = "EmailServiceTaskTest." + testNames[i] + ".bpmn20.xml";
      File file = new File(PACKAGE_DIR, fileName);
      Validate.isTrue(file.exists(),
          "file <" + fileName + "> does not exist in dir " + PACKAGE_DIR.getAbsolutePath());
      files[i] = file;
    }
    return files;
  }

  public String[] getFilePathes() {
    File[] files = getFiles();
    String[] pathes = new String[files.length];
    for (int i = 0; i < pathes.length; i++) {
      pathes[i] = files[i].getAbsolutePath();
    }
    return pathes;
  }

  public File getNotExistingFile() {
    String fileName = "not-existing-file";
    File file = new File(PACKAGE_DIR, fileName);
    Validate.isTrue(!file.exists(), "file <" + fileName + "> does exist in dir "
          + PACKAGE_DIR.getAbsolutePath());
    return file;
  }

  public DataSource getDataSource(String content, String name) {
    return new StringDataSource(content, name);
  }

  private static class StringDataSource implements DataSource {
    private final String content;
    private final String contentType = "text/plain; charset=UTF-8";
    private final String name;

    public StringDataSource(String content, String name) {
      this.content = content;
      this.name = name;
    }

    @Override
    public String getContentType() { return contentType; }

    @Override
    public InputStream getInputStream() {
      try {
        return new ByteArrayInputStream(content.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // this should not happen, since utf-8 is supported in stock jdk, but anyway
        return new ByteArrayInputStream(content.getBytes());
      }
    }

    @Override
    public String getName() { return name; }

    @Override
    public OutputStream getOutputStream() {
      throw new RuntimeException("getOutputStream is not supported");
    }
  }
}
