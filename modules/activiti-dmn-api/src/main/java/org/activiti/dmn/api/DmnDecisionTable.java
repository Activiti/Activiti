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
package org.activiti.dmn.api;


/**
 * An object structure representing an executable decision table 
 * 
 * @author Tijs Rademakers
 * @author Joram Barez
 */
public interface DmnDecisionTable {

  /** unique identifier */
  String getId();

  /**
   * category name of this decision table
   */
  String getCategory();

  /** label used for display purposes */
  String getName();

  /** unique name for all versions this decision table */
  String getKey();

  /** description of this decision table **/
  String getDescription();

  /** version of this decision table */
  int getVersion();

  /**
   * name of {@link DmnRepositoryService#getResourceAsStream(String, String) the resource} of this decision table.
   */
  String getResourceName();

  /** The deployment in which this decision table is contained. */
  String getDeploymentId();
  
  /** The parent deployment in which this decision table is contained. */
  String getParentDeploymentId();

  /** The tenant identifier of this decision table */
  String getTenantId();

}
