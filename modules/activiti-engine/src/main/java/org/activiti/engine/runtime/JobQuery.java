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

import org.activiti.engine.ActivitiException;


/**
 * Allows programmatic querying of {@link Job}s.
 * 
 * @author Joram Barrez
 */
public interface JobQuery {
  
  /** Only select jobs which exists for the given process instance id. **/
  JobQuery processInstanceId(String processInstanceId);

  /** Only select jobs which have retries left */
  JobQuery withRetriesLeft();

  /** Only select jobs which are executable **/
  JobQuery executable();

  //JobQuery timers();
  
  //JobQuery messages();
  
  /** Executes the query and returns the number of results */
  long count();
  
  /**
   * Executes the query and returns the {@link Job}.
   * @throws ActivitiException when the query results in more 
   * than one job. 
   */
  Job singleResult();
  
  /** Executes the query and get a list of {@link Job}s as the result. */
  List<Job> list();
  
  /** Executes the query and get a list of {@link Job}s as the result. */
  List<Job> listPage(int firstResult, int maxResults);

}
