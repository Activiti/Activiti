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
package org.activiti.image.util;

import java.net.URL;


/**
 * @author Tijs Rademakers
 */
public abstract class ReflectUtil {

  public static URL getResource(String name) {
    return getResource(name, null);
  }
  
  public static URL getResource(String name, ClassLoader customClassLoader) {
    URL resourceURL = null;
    if (customClassLoader != null) {
      resourceURL = customClassLoader.getResource(name);
    }
    
    if (resourceURL == null) {
      // Try the current Thread context classloader
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null) {
        resourceURL = classLoader.getResource(name);
      }
      
      if (resourceURL == null) {
        classLoader = ReflectUtil.class.getClassLoader();
        resourceURL = classLoader.getResource(name);
      }
    }
    return resourceURL;
   }
}
