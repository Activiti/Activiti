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

package org.activiti.kickstart.util;

/**
 * @author Joram Barrez
 */
public class ExpressionUtil {
  
  public static String replaceWhiteSpaces(String original) {
    String s = new String(original);
    int indexStart = s.indexOf("${");
    while (indexStart != -1) {
      int indexClose = s.indexOf("}", indexStart);
      if (indexClose != -1) {
        String expression = s.substring(indexStart + 2, indexClose);
        s = s.replace("{" + expression + "}", "{" + expression.replace(" ", "") + "}");
        indexStart = s.indexOf("${", indexClose);
      } else {
        indexStart = indexStart + 1;
      }
    }
    
    return s;
  }
  
  
}
