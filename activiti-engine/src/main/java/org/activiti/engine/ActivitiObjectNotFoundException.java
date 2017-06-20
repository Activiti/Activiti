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

package org.activiti.engine;

/**
 * An exception indicating that the object that is required or actioned on does not exist.
 * 

 */
public class ActivitiObjectNotFoundException extends ActivitiException {

  private static final long serialVersionUID = 1L;

  private Class<?> objectClass;

  public ActivitiObjectNotFoundException(String message) {
    super(message);
  }

  public ActivitiObjectNotFoundException(String message, Class<?> objectClass) {
    this(message, objectClass, null);
  }

  public ActivitiObjectNotFoundException(Class<?> objectClass) {
    this(null, objectClass, null);
  }

  public ActivitiObjectNotFoundException(String message, Class<?> objectClass, Throwable cause) {
    super(message, cause);
    this.objectClass = objectClass;
  }

  /**
   * The class of the object that was not found. Contains the interface-class of the activiti-object that was not found.
   */
  public Class<?> getObjectClass() {
    return objectClass;
  }
}
