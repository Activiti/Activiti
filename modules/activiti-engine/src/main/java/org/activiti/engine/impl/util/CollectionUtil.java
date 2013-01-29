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
package org.activiti.engine.impl.util;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;

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
   * Helper method to easily create a map.
   * 
   * Takes as input a varargs containing the key1, value1, key2, value2, etc.
   * Note: altough an Object, we will cast the key to String internally.
   */
  public static Map<String, Object> map(Object...objects) {
    
    if (objects.length % 2 != 0) {
      throw new ActivitiIllegalArgumentException("The input should always be even since we expect a list of key-value pairs!");
    }
    
    Map<String, Object> map = new HashMap<String, Object>();
    for (int i = 0; i<objects.length; i+=2) {
      map.put((String) objects[i], objects[i+1]);
    }
    
    return map;
  }
  
}
