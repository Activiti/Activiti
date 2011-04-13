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

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceManager extends AbstractHistoricManager {

  @SuppressWarnings("unchecked")
  public void deleteHistoricTaskInstancesByProcessInstanceId(String processInstanceId) {
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      List<String> taskInstanceIds = (List<String>) getDbSqlSession().selectList("selectHistoricTaskInstanceIdsByProcessInstanceId", processInstanceId);
      for (String taskInstanceId: taskInstanceIds) {
        deleteHistoricTaskInstanceById(taskInstanceId);
      }
    }
  }

  public long findHistoricTaskInstanceCountByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricTaskInstanceCountByQueryCriteria", historicTaskInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery, Page page) {
    return getDbSqlSession().selectList("selectHistoricTaskInstancesByQueryCriteria", historicTaskInstanceQuery, page);
  }
  
  public HistoricTaskInstanceEntity findHistoricTaskInstanceById(String taskId) {
    if (taskId == null) {
      throw new ActivitiException("Invalid historic task id : null");
    }
    return (HistoricTaskInstanceEntity) getDbSqlSession().selectOne("selectHistoricTaskInstance", taskId);
  }
  
  public void deleteHistoricTaskInstanceById(String taskId) {
    HistoricTaskInstanceEntity historicTaskInstance = findHistoricTaskInstanceById(taskId);
    if(historicTaskInstance!=null) {
      CommandContext commandContext = Context.getCommandContext();
      
      commandContext
        .getHistoricDetailManager()
        .deleteHistoricDetailsByTaskId(taskId);
        
      commandContext
        .getCommentManager()
        .deleteCommentsByTaskId(taskId);
      
      getDbSqlSession().delete(HistoricTaskInstanceEntity.class, taskId);
    }
  }

  public void markTaskInstanceEnded(String taskId, String deleteReason) {
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.markEnded(deleteReason);
      }
    }
  }

  public void setTaskAssignee(String taskId, String assignee) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setAssignee(assignee);
      }
    }
  }

  public void setTaskOwner(String taskId, String owner) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setOwner(owner);
      }
    }
  }

  public void setTaskName(String id, String taskName) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setName(taskName);
      }
    }
  }

  public void setTaskDescription(String id, String description) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setDescription(description);
      }
    }
  }

  public void setTaskDueDate(String id, Date dueDate) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setDueDate(dueDate);
      }
    }
  }

  public void setTaskPriority(String id, int priority) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setPriority(priority);
      }
    }
  }

  public void setTaskParentTaskId(String id, String parentTaskId) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setParentTaskId(parentTaskId);
      }
    }
  }
}
