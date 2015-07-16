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
package org.activiti.camel;


/**
 * @author Saeid Mirzaei  
 */

public class BooleanStringUtils {
  public static boolean isBoolean(String st) {
    if (st == null)
      return false;
    String lower = st.toLowerCase();
    return lower.equals("true") || lower.equals("false");
  }
}
