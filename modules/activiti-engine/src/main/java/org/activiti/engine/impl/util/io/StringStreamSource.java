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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.activiti.engine.ActivitiException;

/**
 * @author Tom Baeyens
 */
public class StringStreamSource implements StreamSource {

  String string;
  String byteArrayEncoding = "utf-8";

  public StringStreamSource(String string) {
    this.string = string;
  }

  public StringStreamSource(String string, String byteArrayEncoding) {
    this.string = string;
    this.byteArrayEncoding = byteArrayEncoding;
  }

  public InputStream getInputStream() {
    try {
      return new ByteArrayInputStream(byteArrayEncoding == null ? string.getBytes() : string.getBytes(byteArrayEncoding));
    } catch (UnsupportedEncodingException e) {
      throw new ActivitiException("Unsupported enconding for string", e);
    }
  }

  public String toString() {
    return "String";
  }
}
