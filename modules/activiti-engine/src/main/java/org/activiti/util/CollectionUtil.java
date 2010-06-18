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
package org.activiti.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * helper/convience methods for working with collections.
 * 
 * @author Joram Barrez
 */
public class CollectionUtil {

  // No need to instantiate
  private CollectionUtil() {}

  /**
   * Helper method that creates a singleton map.
   * 
   * Alternative for Collections.singletonMap(), since that method returns a
   * generic typed map <K,T> depending on the input type, but we often need a
   * <String, Object> map.
   */
  public static Map<String, Object> singletonMap(String key, Object value) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(key, value);
    return map;
  }

  /**
   * Produces a readable string of the given collection of strings.
   */
  public static String toReadableString(Collection<String> elements) {
    StringBuilder strb = new StringBuilder();    
    for (String element : elements) {
      strb.append(element);
      strb.append(", ");
    }
    return strb.delete(strb.length() - 2, strb.length()).toString();
  }
  
}
