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
package org.activiti.upgrade;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DatabaseFormatter {

  static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss''");

  public String formatDate(Date date) {
    return defaultDateFormat.format(date);
  }

  public String formatBoolean(boolean b) {
    return Boolean.toString(b);
  }

  public String formatBinary(byte[] bytes) {
    StringBuffer sb = new StringBuffer();
    sb.append("X'");
    appendBytesInHex(sb, bytes);
    sb.append("'");
    return sb.toString();
  }
  
  protected void appendBytesInHex(StringBuffer sb, byte[] bytes) {
    for (byte b : bytes) {
      sb.append(String.format("%02X", b));
    }
  }

}
