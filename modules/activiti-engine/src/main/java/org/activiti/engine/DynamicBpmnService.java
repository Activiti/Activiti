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

import com.fasterxml.jackson.databind.node.ObjectNode;

/** Service providing access to the repository of process definitions and deployments.
 * 
 * @author Tijs Rademakers
 */
public interface DynamicBpmnService {

  ObjectNode getProcessDefinitionInfo(String processDefinitionId);
  
  void saveProcessDefinitionInfo(String processDefinitionId, ObjectNode infoNode);
  
  ObjectNode changeClassName(String id, String className);
  
  void changeClassName(String id, String className, ObjectNode infoNode);
  
  ObjectNode changeExpression(String id, String expression);
  
  void changeExpression(String id, String expression, ObjectNode infoNode);
  
  ObjectNode changeDelegateExpression(String id, String expression);
  
  void changeDelegateExpression(String id, String expression, ObjectNode infoNode);
  
  ObjectNode changeFormKey(String id, String formKey);
  
  void changeFormKey(String id, String formKey, ObjectNode infoNode);
  
  ObjectNode getElementProperties(String id, ObjectNode infoNode);

}
