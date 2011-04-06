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
package org.activiti.engine.impl.cmd;

import java.util.Collection;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.impl.HistoricDetailQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoricDetailEntity;
import org.activiti.engine.impl.history.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.ByteArrayEntity;
import org.activiti.engine.impl.task.AttachmentEntity;
import org.activiti.engine.impl.task.TaskEntity;


/**
 * @author Joram Barrez
 */
public class DeleteTaskCmd implements Command<Void> {
  
  protected String taskId;
  protected Collection<String> taskIds;
  protected boolean cascade;
  
  public DeleteTaskCmd(String taskId, boolean cascade) {
    this.taskId = taskId;
    this.cascade = cascade;
  }
  
  public DeleteTaskCmd(Collection<String> taskIds, boolean cascade) {
    this.taskIds = taskIds;
    this.cascade = cascade;
  }

  public Void execute(CommandContext commandContext) {
    if (taskId != null) {
      deleteTask(commandContext, taskId);
    } else if (taskIds != null) {
        for (String taskId : taskIds) {
          deleteTask(commandContext, taskId);
        }   
    } else {
      throw new ActivitiException("taskId and taskIds are null");
    }
    
    
    return null;
  }

  @SuppressWarnings("unchecked")
  protected void deleteTask(CommandContext commandContext, String taskId) {
    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);
    if (task!=null) {
      task.delete(TaskEntity.DELETE_REASON_DELETED);
    }
    if (cascade) {
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
      if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        dbSqlSession.delete(HistoricTaskInstanceEntity.class, taskId);
        List<HistoricDetail> historicTaskDetails = new HistoricDetailQueryImpl(commandContext)
          .taskId(taskId)
          .list();
        for (HistoricDetail historicTaskDetail: historicTaskDetails) {
          dbSqlSession.delete(HistoricDetailEntity.class, historicTaskDetail.getId());
        }
      }
      if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
        Context
          .getCommandContext()
          .getCommentManager()
          .deleteCommentsByTaskId(taskId);
        
        List<AttachmentEntity> attachments = dbSqlSession.selectList("selectAttachmentsByTaskId", taskId);
        for (AttachmentEntity attachment: attachments) {
          String contentId = attachment.getContentId();
          if (contentId!=null) {
            dbSqlSession.delete(ByteArrayEntity.class, contentId);
          }
          dbSqlSession.delete(AttachmentEntity.class, attachment.getId());
        }
      }
    }
  }
}
