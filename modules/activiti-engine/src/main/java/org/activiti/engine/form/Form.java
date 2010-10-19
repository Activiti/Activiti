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

import java.util.Map;


/**
 * @author Tom Baeyens
 */
public interface Form {

  String getFormKey();
  String getDeploymentId();
  
  Map<String, Object> getProperties();
  Object getProperty(String propertyName);
  void setProperty(String propertyName, Object propertyValue);
}
