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

package org.activiti.engine.impl.history.handler;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.delegate.EventListener;
import org.activiti.engine.impl.pvm.delegate.EventListenerExecution;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.util.ClockUtil;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceEndHandler implements EventListener {

  public void notify(EventListenerExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity);

    Date endTime = ClockUtil.getCurrentTime();
    long durationInMillis = endTime.getTime() - historicActivityInstance.getStartTime().getTime();
    historicActivityInstance.setEndTime(endTime);
    historicActivityInstance.setDurationInMillis(durationInMillis);
  }

  public static HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution) {
    CommandContext commandContext = CommandContext.getCurrent();

    String executionId = execution.getId();
    String activityId = execution.getActivityId();

    // TODO search for the historic activity instance in the dbsqlsession cache
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = dbSqlSession.findInCache(HistoricActivityInstanceEntity.class);
    for (HistoricActivityInstanceEntity cachedHistoricActivityInstance: cachedHistoricActivityInstances) {
      if ( executionId.equals(cachedHistoricActivityInstance.getExecutionId())
           && (activityId.equals(cachedHistoricActivityInstance.getActivityId()))
           && (cachedHistoricActivityInstance.getEndTime()==null)
         ) {
        return cachedHistoricActivityInstance;
      }
    }
    
    List<HistoricActivityInstance> historicActivityInstances = new HistoricActivityInstanceQueryImpl()
      .executionId(executionId)
      .activityId(activityId)
      .onlyOpen()
      .executeList(commandContext, new Page(0, 1));
    
    if (!historicActivityInstances.isEmpty()) {
      return (HistoricActivityInstanceEntity) historicActivityInstances.get(0);
    }
    
    if (execution.getParentId()!=null) {
      return findActivityInstance((ExecutionEntity) execution.getParent());
    }
    
    throw new ActivitiException("no existing history activity entity found for execution "+executionId+" in activity "+activityId);
  }
}
