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

package org.activiti.pvm.impl.util;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public abstract class ClassNameUtil {

  protected static final Map<Class<?>, String> cachedNames = new HashMap<Class<?>, String>();  
  
  public static String getClassNameWithoutPackage(Object object) {
    return getClassNameWithoutPackage(object.getClass());
  }

  public static String getClassNameWithoutPackage(Class<?> clazz) {
    String unqualifiedClassName = cachedNames.get(clazz);
    if (unqualifiedClassName==null) {
      String fullyQualifiedClassName = clazz.getName();
      unqualifiedClassName = fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf('.')+1);
      cachedNames.put(clazz, unqualifiedClassName);
    }
    return unqualifiedClassName;
  }
}
