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
package org.activiti.engine.impl.util.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.activiti.engine.ActivitiException;

/**


 */
public class InputStreamSource implements StreamSource {

  // This class is used for bpmn parsing.
  // The bpmn parsers needs to go over the stream at least twice:
  // Once for the schema validation and once for the parsing itself.
  // So we keep the content of the inputstream in memory so we can
  // re-read it.

  protected BufferedInputStream inputStream;
  protected byte[] bytes;

  public InputStreamSource(InputStream inputStream) {
    this.inputStream = new BufferedInputStream(inputStream);
  }

  public InputStream getInputStream() {
    if (bytes == null) {
      try {
        bytes = getBytesFromInputStream(inputStream);
      } catch (IOException e) {
        throw new ActivitiException("Could not read from inputstream", e);
      }
    }
    return new BufferedInputStream(new ByteArrayInputStream(bytes));
  }

  public String toString() {
    return "InputStream";
  }

  public byte[] getBytesFromInputStream(InputStream inStream) throws IOException {
    long length = inStream.available();
    byte[] bytes = new byte[(int) length];

    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length && (numRead = inStream.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }

    if (offset < bytes.length) {
      throw new ActivitiException("Could not completely read inputstream ");
    }

    // Close the input stream and return bytes
    inStream.close();
    return bytes;
  }

}
