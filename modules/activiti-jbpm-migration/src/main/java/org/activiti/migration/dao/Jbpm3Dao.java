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
package org.activiti.migration.dao;

import java.util.List;

import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;


/**
 * @author Joram Barrez
 */
public interface Jbpm3Dao {
  
  List<ProcessDefinition> getAllProcessDefinitions();
  
  List<Node> getNodes(ProcessDefinition processDefinition);
  
  // From and to are eagerly fetched
  List<Transition> getOutgoingTransitions(Node node);
  
  void close();

}
