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


public class DatabaseFormatterOracle extends DatabaseFormatter {

  static SimpleDateFormat oracleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");

  public String formatDate(Date date) {
    StringBuffer sb = new StringBuffer();
    sb.append("to_timestamp('");
    sb.append(oracleDateFormat.format(date));
    sb.append("', 'YYYY-MM-DD HH24:MI:SS.FF')");
    return sb.toString();
  }

  @Override
  public String formatBoolean(boolean b) {
    return (b ? "1" : "0");
  }

  @Override
  public String formatBinary(byte[] bytes) {
    StringBuffer sb = new StringBuffer();
    sb.append("hextoraw('");
    appendBytesInHex(sb, bytes);
    sb.append("')");
    return sb.toString();
  }
}
