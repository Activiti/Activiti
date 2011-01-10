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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceEndHandler implements ExecutionListener {

  public void notify(ExecutionListenerExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity);
    historicActivityInstance.markEnded(null);
  }

  public static HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution) {
    CommandContext commandContext = CommandContext.getCurrent();

    String executionId = execution.getId();
    String activityId = execution.getActivityId();

    // search for the historic activity instance in the dbsqlsession cache
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
      .unfinished()
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
