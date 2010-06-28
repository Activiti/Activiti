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
package org.activiti;


/**
 * @author Tom Baeyens
 */
public enum DbSchemaStrategy {
  
  /**
   * Creates the schema when the process engine is being created.
   * 
   * Probably only useful in testing (eg. when testing rebooting the process engine).
   */
  CREATE,

  /** creates the schema when the process engine is being created and 
   * drops the schema when the process engine is being closed.
   */
  CREATE_DROP,
  
  /** checks the version of the DB schema against the library when 
   * the process engine is being created.
   */
  CHECK_VERSION
}
