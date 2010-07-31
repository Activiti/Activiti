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


/**
 * @author Tom Baeyens
 */
public abstract class ClassNameUtil {

  public static String getClassNameWithoutPackage(Object object) {
    return getClassNameWithoutPackage(object.getClass());
  }
  public static String getClassNameWithoutPackage(Class<?> clazz) {
    String fullyQualifiedClassName = clazz.getName();
    return fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf('.')+1);
  }

}
