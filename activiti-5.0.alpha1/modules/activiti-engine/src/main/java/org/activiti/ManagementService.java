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
package org.activiti;

import java.util.Map;

import org.activiti.mgmt.TablePage;


/**
 * is a service for admin and maintenance operations on the process engine.
 * 
 * These operations will typically not be used in a workflow driven application,
 * but are used in for example the operational console.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ManagementService {

  /**
   * @return The mapping containing {table name, row count} entries of the
   *         Activiti database schema.
   */
  Map<String, Long> getTableCount();
  
  /**
   * Retrieves a page of data from the given table.
   * 
   * @param offset The record number of the first element of the page.
   *               Note that this is zero-based (ie. the first element has index '0') 
   * @param maxResults The number of elements that the page maximum can contain.
   */
  TablePage getTablePage(String tableName, int offset, int maxResults);
  
}
