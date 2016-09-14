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
package org.activiti.form.api;


/**
 * An object structure representing a form
 * 
 * At deploy time, the engine will then parse the form definition files to an form instance of this class.
 * 
 * @author Tijs Rademakers
 * @author Joram Barez
 */
public interface Form {

  /** unique identifier */
  String getId();

  /**
   * category name of the form
   */
  String getCategory();

  /** label used for display purposes */
  String getName();

  /** unique name for all versions this form */
  String getKey();

  /** description of this form **/
  String getDescription();

  /** version of this form */
  int getVersion();

  /**
   * name of {@link FormRepositoryService#getResourceAsStream(String, String) the resource} of this process definition.
   */
  String getResourceName();

  /** The deployment in which this form is contained. */
  String getDeploymentId();
  
  /** The parent deployment id in which this form is contained. */
  String getParentDeploymentId();

  /** The tenant identifier of this form */
  String getTenantId();

}
