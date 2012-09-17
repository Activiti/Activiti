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

package org.activiti.engine.query;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

/**
 * Describes basic methods for doing native queries
 * 
 * @author Bernd Ruecker (camunda)
 */
public interface NativeQuery<T extends NativeQuery< ? , ? >, U extends Object> {

  /**
   * Change select clause (default is "*") e.g. if you have joined 
   * multiple tables and need more control
   */
  T select(String selectClause);

  /**
   * set mandatory from clause for query (SQL)
   */
  T from(String fromClause);
  
  /**
   * Add parameter to be replaced in query for index, e.g. :param1, :myParam, ...
   */
  T parameter(String name, Object value);

  /** Executes the query and returns the number of results */
  long count();

  /**
   * Executes the query and returns the resulting entity or null if no
   * entity matches the query criteria.
   * @throws ActivitiException when the query results in more than one
   * entities.
   */
  U singleResult();

  /** Executes the query and get a list of entities as the result. */
  List<U> list();

  /** Executes the query and get a list of entities as the result. */
  List<U> listPage(int firstResult, int maxResults);
}
