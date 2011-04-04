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

package org.activiti.engine.impl.persistence.mgr;

import java.util.List;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.VariableInstanceEntity;
import org.activiti.engine.impl.task.IdentityLinkEntity;
import org.activiti.engine.impl.task.TaskEntity;


/**
 * @author Tom Baeyens
 */
public class TaskManager extends AbstractManager {

  public void deleteTask(TaskEntity task, String deleteReason) {
    if (!task.isDeleted()) {
      task.setDeleted(true);
      
      String taskId = task.getId();
      
      // cascade deletion to task assignments
      IdentityLinkManager identityLinkManager = Context
        .getCommandContext()
        .getIdentityLinkManager();
      List<IdentityLinkEntity> identityLinks = identityLinkManager.findIdentityLinksByTaskId(taskId);
      for (IdentityLinkEntity identityLink: identityLinks) {
        identityLinkManager.deleteIdentityLink(identityLink);
      }

      VariableInstanceManager variableInstanceManager = Context
        .getCommandContext()
        .getVariableInstanceManager();
      
      List<VariableInstanceEntity> variableInstances = variableInstanceManager.findVariableInstancesByTaskId(taskId);
        
      for (VariableInstanceEntity variableInstance: variableInstances) {
        variableInstanceManager.deleteVariableInstance(variableInstance);
      }

      CommandContext commandContext = Context.getCommandContext();
      DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = dbSqlSession
          .selectById(HistoricTaskInstanceEntity.class, task.getId());
        if (historicTaskInstance!=null) {
          historicTaskInstance.markEnded(deleteReason);
        }
      }

      getPersistenceSession().delete(TaskEntity.class, task.getId());
    }
  }

}
