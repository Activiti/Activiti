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

package org.activiti.engine.impl.persistence;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.entity.AttachmentEntityManager;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityManager;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManager;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.MembershipIdentityManager;
import org.activiti.engine.impl.persistence.entity.ModelEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class AbstractManager implements Session {
  
  public void insert(PersistentObject persistentObject) {
    getDbSqlSession().insert(persistentObject);
  }

  public void delete(PersistentObject persistentObject) {
    getDbSqlSession().delete(persistentObject);
  }

  protected DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }

  protected <T> T getSession(Class<T> sessionClass) {
    return Context.getCommandContext().getSession(sessionClass);
  }

  protected DeploymentEntityManager getDeploymentManager() {
    return getSession(DeploymentEntityManager.class);
  }

  protected ResourceEntityManager getResourceManager() {
    return getSession(ResourceEntityManager.class);
  }
  
  protected ByteArrayEntityManager getByteArrayManager() {
    return getSession(ByteArrayEntityManager.class);
  }
  
  protected ProcessDefinitionEntityManager getProcessDefinitionManager() {
    return getSession(ProcessDefinitionEntityManager.class);
  }
  
  protected ProcessDefinitionInfoEntityManager getProcessDefinitionInfoManager() {
    return getSession(ProcessDefinitionInfoEntityManager.class);
  }
  
  protected ModelEntityManager getModelManager() {
    return getSession(ModelEntityManager.class);
  }

  protected ExecutionEntityManager getProcessInstanceManager() {
    return getSession(ExecutionEntityManager.class);
  }

  protected TaskEntityManager getTaskManager() {
    return getSession(TaskEntityManager.class);
  }

  protected IdentityLinkEntityManager getIdentityLinkManager() {
    return getSession(IdentityLinkEntityManager.class);
  }
  
  protected EventSubscriptionEntityManager getEventSubscriptionManager() {
  	return (getSession(EventSubscriptionEntityManager.class));
  }

  protected VariableInstanceEntityManager getVariableInstanceManager() {
    return getSession(VariableInstanceEntityManager.class);
  }

  protected HistoricProcessInstanceEntityManager getHistoricProcessInstanceManager() {
    return getSession(HistoricProcessInstanceEntityManager.class);
  }

  protected HistoricDetailEntityManager getHistoricDetailManager() {
    return getSession(HistoricDetailEntityManager.class);
  }

  protected HistoricActivityInstanceEntityManager getHistoricActivityInstanceManager() {
    return getSession(HistoricActivityInstanceEntityManager.class);
  }
  
  protected HistoricVariableInstanceEntityManager getHistoricVariableInstanceManager() {
    return getSession(HistoricVariableInstanceEntityManager.class);
  }
  
  protected HistoricTaskInstanceEntityManager getHistoricTaskInstanceManager() {
    return getSession(HistoricTaskInstanceEntityManager.class);
  }
  
  protected HistoricIdentityLinkEntityManager getHistoricIdentityLinkEntityManager() {
    return getSession(HistoricIdentityLinkEntityManager.class);
  }
  
  protected UserIdentityManager getUserIdentityManager() {
    return getSession(UserIdentityManager.class);
  }
  
  protected GroupIdentityManager getGroupIdentityManager() {
    return getSession(GroupIdentityManager.class);
  }
  
  protected IdentityInfoEntityManager getIdentityInfoManager() {
    return getSession(IdentityInfoEntityManager.class);
  }
  
  protected MembershipIdentityManager getMembershipIdentityManager() {
    return getSession(MembershipIdentityManager.class);
  }
  
  protected AttachmentEntityManager getAttachmentManager() {
    return getSession(AttachmentEntityManager.class);
  }
  
  protected HistoryManager getHistoryManager() {
    return getSession(HistoryManager.class);
  }
  
  protected ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
  	return Context.getProcessEngineConfiguration();
  }
  
  public void close() {
  }

  public void flush() {
  }
}
