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
package org.activiti.engine.impl.persistence.entity;

import java.util.Collection;
import java.util.List;

import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;

/**
 * @author Tijs Rademakers
 * @author Vasile Dirla
 */
public interface TimerJobEntityManager extends EntityManager<TimerJobEntity> {

  List<TimerJobEntity> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page);

  long findJobCountByQueryCriteria(JobQueryImpl jobQuery);

  TimerJobEntity createTimer(JobEntity timerEntity);
  
  void createAndCalculateNextTimer(JobEntity timerEntity);

  List<TimerJobEntity> selectTimerJobsToDueDate(Page page);

  List<TimerJobEntity> findJobsByTypeAndProcessDefinitionId(String type, String id);

  Collection<TimerJobEntity> findJobsByExecutionId(String id);
}