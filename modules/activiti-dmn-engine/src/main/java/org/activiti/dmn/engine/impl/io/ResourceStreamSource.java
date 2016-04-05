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
package org.activiti.dmn.engine.impl.io;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.activiti.dmn.engine.ActivitiDmnException;


/**
 * @author Joram Barrez
 */
public class ResourceStreamSource implements StreamSource {

  String resource;
  
  public ResourceStreamSource(String resource) {
    this.resource = resource;
  }

  public InputStream getInputStream() {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resource);
    if (inputStream == null) {
      throw new ActivitiDmnException("resource '"+resource+"' doesn't exist");
    }
    return new BufferedInputStream(inputStream);
  }

  public String toString() {
    return "Resource["+resource+"]";
  }
}
