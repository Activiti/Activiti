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
package org.activiti.migration.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.activiti.engine.ActivitiException;

/**
 * @author Joram Barrez
 */
public class ZipUtil {
  
  public static ZipInputStream createZipInputStream(Map<String, byte[]> files) {
    return new ZipInputStream(new ByteArrayInputStream(createZipFile(files)));
  }
  
  public static byte[] createZipFile(Map<String, byte[]> files) {
    ZipOutputStream zipfile = null;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      zipfile = new ZipOutputStream(bos);
      for (String fileName : files.keySet()) {
        ZipEntry zipentry = new ZipEntry(fileName);
        zipfile.putNextEntry(zipentry);
        zipfile.write(files.get(fileName));
      }
      zipfile.flush();
      bos.flush();
      zipfile.close();
      return bos.toByteArray();
    } catch (Exception e) {
      throw new ActivitiException("Couldn't create zip file", e);
    } 
  }

}
