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
package org.activiti.engine.task;

import java.util.List;


/**
 * builds dynamic search queries for tasks.
 * 
 * @author Joram Barrez
 */
public interface TaskQuery {
  
  final String PROPERTY_NAME = "NAME_";
  
  TaskQuery name(String name);
  
  TaskQuery assignee(String assignee);
  
  TaskQuery candidateUser(String candidateUser);
  
  TaskQuery candidateGroup(String candidateGroup);
  
  TaskQuery processInstanceId(String processInstanceId);
  
  TaskQuery executionId(String executionId);
  
  TaskQuery orderAsc(String property);
  
  TaskQuery orderDesc(String property);
  
  long count();
  
  Task singleResult();
  
  List<Task> list();
  
  List<Task> listPage(int start, int size);

}
