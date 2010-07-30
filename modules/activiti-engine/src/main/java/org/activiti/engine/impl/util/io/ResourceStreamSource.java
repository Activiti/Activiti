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
package org.activiti.engine.impl.util.io;

import java.io.InputStream;

import org.activiti.engine.ActivitiException;


/**
 * @author Tom Baeyens
 */
public class ResourceStreamSource implements StreamSource {

  String resource;
  ClassLoader classLoader;
  
  public ResourceStreamSource(String resource) {
    this.resource = resource;
  }

  public ResourceStreamSource(String resource, ClassLoader classLoader) {
    this.resource = resource;
    this.classLoader = classLoader;
  }

  public InputStream getInputStream() {
    if (classLoader==null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }
    InputStream inputStream = classLoader.getResourceAsStream(resource);
    if (inputStream==null) {
      throw new ActivitiException("resource '"+resource+"' doesn't exist");
    }
    return inputStream;
  }

  public String toString() {
    return "Resource["+resource+"]";
  }
}
