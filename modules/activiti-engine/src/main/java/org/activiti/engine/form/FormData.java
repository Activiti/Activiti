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

package org.activiti.engine.form;

import java.util.List;


/** Contains all information for displaying a form and serves as 
 * base interface for {@link StartFormData} and {@link TaskFormData}
 * 
 * @author Tom Baeyens
 */
public interface FormData {

  /** User defined reference to a form. In the Explorer app, it is 
   * assumed that the form key specifies a resource in the deployment 
   * which is the template for the form.  But users are free to 
   * use this property differently. */
  String getFormKey();

  /** The deployment id of the process definition to which this form is related 
   *  */
  String getDeploymentId();
  
  /** Properties containing the dynamic information that needs to be displayed in the form. */
  List<FormProperty> getFormProperties();
}
