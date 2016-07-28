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

package org.activiti.form.api;

import java.util.Date;
import java.util.Set;

/**
 * Allows programmatic querying of {@link SubmittedForm}s.
 * 
 * @author Tijs Rademakers
 */
public interface SubmittedFormQuery extends Query<SubmittedFormQuery, SubmittedForm> {

  /**
   * Only select submitted forms with the given id.
   */
  SubmittedFormQuery id(String id);
  
  /**
   * Only select submitted forms with the given ids.
   */
  SubmittedFormQuery ids(Set<String> ids);
  
  /**
   * Only select submitted forms with the given form id.
   */
  SubmittedFormQuery formId(String formId);

  /**
   * Only select submitted forms with a form id like the given string.
   */
  SubmittedFormQuery formIdLike(String formIdLike);

  /**
   * Only select submitted forms with the given task id.
   */
  SubmittedFormQuery taskId(String taskId);

  /**
   * Only select submitted forms with a task id like the given string.
   */
  SubmittedFormQuery taskIdLike(String taskIdLike);
  
  /**
   * Only select submitted forms with the given process instance id.
   */
  SubmittedFormQuery processInstanceId(String processInstanceId);

  /**
   * Only select submitted forms with a process instance id like the given string.
   */
  SubmittedFormQuery processInstanceIdLike(String processInstanceIdLike);
  
  /**
   * Only select submitted forms with the given process definition id.
   */
  SubmittedFormQuery processDefinitionId(String processDefinitionId);

  /**
   * Only select submitted forms with a process definition id like the given string.
   */
  SubmittedFormQuery processDefinitionIdLike(String processDefinitionIdLike);
  
  /**
   * Only select submitted forms submitted on the given time
   */
  SubmittedFormQuery submittedDate(Date submittedDate);
  
  /**
   * Only select submitted forms submitted before the given time
   */
  SubmittedFormQuery submittedDateBefore(Date beforeTime);

  /**
   * Only select submitted forms submitted after the given time
   */
  SubmittedFormQuery submittedDateAfter(Date afterTime);
  
  /**
   * Only select submitted forms with the given submitted by value.
   */
  SubmittedFormQuery submittedBy(String submittedBy);

  /**
   * Only select submitted forms with a submitted by like the given string.
   */
  SubmittedFormQuery submittedByLike(String submittedByLike);

  /**
   * Only select submitted forms that have the given tenant id.
   */
  SubmittedFormQuery deploymentTenantId(String tenantId);

  /**
   * Only select submitted forms with a tenant id like the given one.
   */
  SubmittedFormQuery deploymentTenantIdLike(String tenantIdLike);

  /**
   * Only select submitted forms that do not have a tenant id.
   */
  SubmittedFormQuery deploymentWithoutTenantId();

  // sorting ////////////////////////////////////////////////////////

  /**
   * Order by submitted date (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  SubmittedFormQuery orderBySubmittedDate();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  SubmittedFormQuery orderByTenantId();
}
