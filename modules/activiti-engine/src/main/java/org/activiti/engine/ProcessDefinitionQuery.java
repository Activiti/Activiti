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

import java.util.List;


/**
 * @author Tom Baeyens
 */
public interface ProcessDefinitionQuery {
  
  String PROPERTY_ID = "ID_";
  String PROPERTY_KEY = "KEY_";
  String PROPERTY_VERSION = "VERSION_";

  ProcessDefinitionQuery deploymentId(String deploymentId);
  
  ProcessDefinitionQuery orderAsc(String property);
  
  ProcessDefinitionQuery orderDesc(String property);

  long count();
  
  ProcessDefinition singleResult();
  
  List<ProcessDefinition> list();
  
  List<ProcessDefinition> listPage(int start, int size);
}
