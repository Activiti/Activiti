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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.ActivitiIllegalArgumentException;

/**
 * helper/convenience methods for working with collections.
 *
 */
public class CollectionUtil {

  // No need to instantiate
  private CollectionUtil() {
  }

  /**
   * Helper method that creates a singleton map.
   *
   * Alternative for singletonMap()), since that method returns a generic typed map <K,T> depending on the input type, but we often need a <String, Object> map.
   */
  public static Map<String, Object> singletonMap(String key, Object value) {
    Map<String, Object> map = new HashMap<>();
    map.put(key, value);
    return map;
  }

  /**
   * Helper method to easily create a map with keys of type String and values of type Object. Null values are allowed.
   *
   * @param objects varargs containing the key1, value1, key2, value2, etc. Note: although an Object, we will cast the key to String internally
   * @throws ActivitiIllegalArgumentException when objects are not even or key/value are not expected types
   */
  public static Map<String, Object> map(Object... objects) {
    return mapOfClass(Object.class, objects);
  }

  /**
   * Helper method to easily create a map with keys of type String and values of a given Class. Null values are allowed.
   *
   * @param clazz the target Value class
   * @param objects varargs containing the key1, value1, key2, value2, etc. Note: although an Object, we will cast the key to String internally
   * @throws ActivitiIllegalArgumentException when objects are not even or key/value are not expected types
   */
  public static <T> Map<String, T> mapOfClass(Class<T> clazz, Object... objects) {
    if (objects.length % 2 != 0) {
        throw new ActivitiIllegalArgumentException("The input should always be even since we expect a list of key-value pairs!");
    }

    Map<String, T> map = new HashMap();
    for (int i = 0; i < objects.length; i += 2) {
        int keyIndex = i;
        int valueIndex = i + 1;
        Object key = objects[keyIndex];
        Object value = objects[valueIndex];
        if (!String.class.isInstance(key)) {
            throw new ActivitiIllegalArgumentException("key at index " + keyIndex + " should be a String but is a " + key.getClass());
        }
        if (value != null && !clazz.isInstance(value)) {
            throw new ActivitiIllegalArgumentException("value at index " + valueIndex + " should be a " + clazz + " but is a " + value.getClass());
        }
        map.put((String) key, (T) value);
    }

    return map;
  }

  public static boolean isEmpty(Collection<?> collection) {
    return (collection == null || collection.isEmpty());
  }

  public static boolean isNotEmpty(Collection<?> collection) {
    return !isEmpty(collection);
  }

}
