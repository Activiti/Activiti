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

package org.activiti.engine.identity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;


/**
 * @author Tom Baeyens
 */
public class Picture implements Serializable {

  private static final long serialVersionUID = 2384375526314443322L;
  
  protected byte[] bytes;
  protected String mimeType;

  public Picture(byte[] bytes, String mimeType) {
    this.bytes = bytes;
    this.mimeType = mimeType;
  }

  public byte[] getBytes() {
    return bytes;
  }
  
  public InputStream getInputStream() {
    return new ByteArrayInputStream(bytes);
  }

  public String getMimeType() {
    return mimeType;
  }
}
