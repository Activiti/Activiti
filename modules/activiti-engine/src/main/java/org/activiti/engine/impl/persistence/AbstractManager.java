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
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.cache.PersistentObjectCache;
import org.activiti.engine.impl.persistence.cache.PersistentObjectCacheImpl;
import org.activiti.engine.impl.persistence.entity.AttachmentEntityManager;
import org.activiti.engine.impl.persistence.entity.AttachmentEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityManager;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManager;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.MembershipEntityManager;
import org.activiti.engine.impl.persistence.entity.ModelEntityManager;
import org.activiti.engine.impl.persistence.entity.ModelEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManagerImpl;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class AbstractManager implements Session {

  protected DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }
  
  protected PersistentObjectCache getPersistentObjectCache() {
    return getSession(PersistentObjectCacheImpl.class);
  }

  protected <T> T getSession(Class<T> sessionClass) {
    return Context.getCommandContext().getSession(sessionClass);
  }

  protected DeploymentEntityManager getDeploymentManager() {
    return getSession(DeploymentEntityManagerImpl.class);
  }

  protected ResourceEntityManager getResourceManager() {
    return getSession(ResourceEntityManagerImpl.class);
  }

  protected ByteArrayEntityManager getByteArrayManager() {
    return getSession(ByteArrayEntityManagerImpl.class);
  }

  protected ProcessDefinitionEntityManager getProcessDefinitionManager() {
    return getSession(ProcessDefinitionEntityManagerImpl.class);
  }

  protected ModelEntityManager getModelManager() {
    return getSession(ModelEntityManagerImpl.class);
  }

  protected ExecutionEntityManager getProcessInstanceManager() {
    return getSession(ExecutionEntityManagerImpl.class);
  }

  protected TaskEntityManager getTaskManager() {
    return getSession(TaskEntityManagerImpl.class);
  }

  protected IdentityLinkEntityManager getIdentityLinkManager() {
    return getSession(IdentityLinkEntityManagerImpl.class);
  }

  protected EventSubscriptionEntityManager getEventSubscriptionManager() {
    return (getSession(EventSubscriptionEntityManagerImpl.class));
  }

  protected VariableInstanceEntityManager getVariableInstanceManager() {
    return getSession(VariableInstanceEntityManagerImpl.class);
  }

  protected HistoricProcessInstanceEntityManager getHistoricProcessInstanceManager() {
    return getSession(HistoricProcessInstanceEntityManagerImpl.class);
  }

  protected HistoricDetailEntityManager getHistoricDetailManager() {
    return getSession(HistoricDetailEntityManagerImpl.class);
  }

  protected HistoricActivityInstanceEntityManager getHistoricActivityInstanceManager() {
    return getSession(HistoricActivityInstanceEntityManagerImpl.class);
  }

  protected HistoricVariableInstanceEntityManager getHistoricVariableInstanceManager() {
    return getSession(HistoricVariableInstanceEntityManagerImpl.class);
  }

  protected HistoricTaskInstanceEntityManager getHistoricTaskInstanceManager() {
    return getSession(HistoricTaskInstanceEntityManagerImpl.class);
  }

  protected HistoricIdentityLinkEntityManager getHistoricIdentityLinkEntityManager() {
    return getSession(HistoricIdentityLinkEntityManagerImpl.class);
  }

  protected UserEntityManager getUserIdentityManager() {
    return getSession(UserEntityManager.class);
  }

  protected GroupEntityManager getGroupIdentityManager() {
    return getSession(GroupEntityManager.class);
  }

  protected IdentityInfoEntityManager getIdentityInfoManager() {
    return getSession(IdentityInfoEntityManagerImpl.class);
  }

  protected MembershipEntityManager getMembershipIdentityManager() {
    return getSession(MembershipEntityManager.class);
  }

  protected AttachmentEntityManager getAttachmentManager() {
    return getSession(AttachmentEntityManagerImpl.class);
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
