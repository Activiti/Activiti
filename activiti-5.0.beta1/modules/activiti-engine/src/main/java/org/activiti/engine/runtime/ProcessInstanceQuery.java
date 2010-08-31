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
package org.activiti.engine.runtime;

import java.util.List;



/** builds dynamic search queries for process instances.
 * 
 * @author Joram Barrez
 */
public interface ProcessInstanceQuery {
  
  ProcessInstanceQuery processInstanceId(String processInstanceId);
  ProcessInstanceQuery processDefinitionKey(String processDefinitionKey);
  
  List<ProcessInstance> list();
  List<ProcessInstance> listPage(int firstResult, int maxResults);
  ProcessInstance singleResult();
  long count();
}
