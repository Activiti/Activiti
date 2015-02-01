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

package org.activiti.explorer.ui.process;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.explorer.ui.process.ProcessDefinitionListQuery.ProcessDefinitionListItem;


/**
 * Class used in {@link ProcessDefinitionListQuery} for filtering.
 * 
 * @author Frederik Heremans
 */
public interface ProcessDefinitionFilter {

  /**
   * Return a query that filters definitions, paging info is applied later on
   * and should not be altered.
   */
  ProcessDefinitionQuery getQuery(RepositoryService repositoryService);
  
  /**
   * Return a query that filters definitions, used for counting total number.
   */
  ProcessDefinitionQuery getCountQuery(RepositoryService repositoryService);
  
  /**
   * @param a {@link ProcessDefinition} resulting from the query
   * @return item representing the definition
   */
  ProcessDefinitionListItem createItem(ProcessDefinition processDefinition);
}
