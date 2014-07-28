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

import java.util.Date;

import org.activiti.engine.query.Query;


/**
 * Allows programmatic querying of {@link Job}s.
 * 
 * @author Joram Barrez
 * @author Falko Menge
 */
public interface JobQuery extends Query<JobQuery, Job> {
  
  /** Only select jobs with the given id */
  JobQuery jobId(String jobId);
  
  /** Only select jobs which exist for the given process instance. **/
  JobQuery processInstanceId(String processInstanceId);
  
  /** Only select jobs which exist for the given execution */ 
  JobQuery executionId(String executionId);
  
  /** Only select jobs which exist for the given process definition id */
  JobQuery processDefinitionId(String processDefinitionid);

  /** Only select jobs which have retries left */
  JobQuery withRetriesLeft();
  
  /** Only select jobs which have no retries left */
  JobQuery noRetriesLeft();

  /** Only select jobs which are executable, 
   * ie. retries &gt; 0 and duedate is null or duedate is in the past **/
  JobQuery executable();

  /** Only select jobs that are timers. 
   * Cannot be used together with {@link #messages()} */
  JobQuery timers();
 
  /** Only select jobs that are messages. 
   * Cannot be used together with {@link #timers()} */
  JobQuery messages();
  
  /** Only select jobs where the duedate is lower than the given date. */
  JobQuery duedateLowerThan(Date date);
  
  /** Only select jobs where the duedate is higher then the given date. */
  JobQuery duedateHigherThan(Date date);
  
  /** Only select jobs where the duedate is lower then the given date.  
   * @deprecated 
   */
  JobQuery duedateLowerThen(Date date);
  
  /** Only select jobs where the duedate is lower then or equals the given date.
   * @deprecated 
   */
  JobQuery duedateLowerThenOrEquals(Date date);
  
  /** Only select jobs where the duedate is higher then the given date.
   * @deprecated 
   */
  JobQuery duedateHigherThen(Date date);
  
  /** Only select jobs where the duedate is higher then or equals the given date.
   * @deprecated 
   */
  JobQuery duedateHigherThenOrEquals(Date date);
  
  /** Only select jobs that failed due to an exception. */
  JobQuery withException();

  /** Only select jobs that failed due to an exception with the given message. */
  JobQuery exceptionMessage(String exceptionMessage);
  
  /**
	 * Only select jobs that have the given tenant id.
	 */
  JobQuery jobTenantId(String tenantId);

	/**
	 * Only select jobs with a tenant id like the given one.
	 */
  JobQuery jobTenantIdLike(String tenantIdLike);
	
	/**
	 * Only select jobs that do not have a tenant id.
	 */
  JobQuery jobWithoutTenantId();

  //sorting //////////////////////////////////////////
  
  /** Order by job id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByJobId();
  
  /** Order by duedate (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByJobDuedate();
  
  /** Order by retries (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByJobRetries();

  /** Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByProcessInstanceId();
  
  /** Order by execution id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByExecutionId();
  
	/** Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByTenantId();
  
}
