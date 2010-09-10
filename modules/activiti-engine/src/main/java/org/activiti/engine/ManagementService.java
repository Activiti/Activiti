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

import java.util.Map;

import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.runtime.JobQuery;



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
   * @return The metadata (column names, column types, etc.) of a certain table. 
   * Returns an empty TableMetaData object when the table does not exist.
   */
  TableMetaData getTableMetaData(String tableName);
 
  /**
   * creates a {@link TablePageQuery} that can be used to fetch {@link TablePage}
   * containing specific sections of table row signalData.
   */
  TablePageQuery createTablePageQuery();
  
  /**
   * Returns a new JobQuery implementation, that can be used
   * to dynamically query the jobs.
   */
  JobQuery createJobQuery();
  
  /** forced synchronous execution of a job for testing purposes. */
  void executeJob(String jobId);
  
}
