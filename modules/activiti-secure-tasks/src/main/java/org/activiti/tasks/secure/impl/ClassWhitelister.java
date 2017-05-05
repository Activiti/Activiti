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
package org.activiti.tasks.secure.impl;

/**
 * @author Bassam Al-Sarori
 */
public interface ClassWhitelister {

  /**
   * Determines whether a class name should be whitlisted or not.
   * 
   * @param fqcn fully qualified class name
   * @return true if provided class should be whitelisted otherwise false. 
   */
  boolean isWhitelisted(String fqcn);

}
