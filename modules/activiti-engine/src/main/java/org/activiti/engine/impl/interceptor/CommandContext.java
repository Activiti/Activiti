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
package org.activiti.engine.impl.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.JobNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.persistence.entity.AttachmentEntityManager;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityManager;
import org.activiti.engine.impl.persistence.entity.CommentEntityManager;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManager;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityManager;
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
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.persistence.entity.MembershipIdentityManager;
import org.activiti.engine.impl.persistence.entity.ModelEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;
import org.activiti.engine.impl.persistence.entity.PropertyEntityManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.engine.impl.persistence.entity.TableDataManager;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.logging.LogMDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Agim Emruli
 * @author Joram Barrez
 */
public class CommandContext {

  private static Logger log = LoggerFactory.getLogger(CommandContext.class);

  protected Command< ? > command;
  protected TransactionContext transactionContext;
  protected Map<Class< ? >, SessionFactory> sessionFactories;
  protected Map<Class< ? >, Session> sessions = new HashMap<Class< ? >, Session>();
  protected Throwable exception = null;
  protected LinkedList<AtomicOperation> nextOperations = new LinkedList<AtomicOperation>();
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected FailedJobCommandFactory failedJobCommandFactory;
	protected List<CommandContextCloseListener> closeListeners;
  protected Map<String, Object> attributes; // General-purpose storing of anything during the lifetime of a command context

  
  public void performOperation(AtomicOperation executionOperation, InterpretableExecution execution) {
    nextOperations.add(executionOperation);
    if (nextOperations.size()==1) {
      try {
        Context.setExecutionContext(execution);
        while (!nextOperations.isEmpty()) {
          AtomicOperation currentOperation = nextOperations.removeFirst();
          if (log.isTraceEnabled()) {
            log.trace("AtomicOperation: {} on {}", currentOperation, this);
          }
          if (execution.getReplacedBy() == null) {
          	currentOperation.execute(execution);
          } else {
          	currentOperation.execute(execution.getReplacedBy());
          }
        }
      } finally {
        Context.removeExecutionContext();
      }
    }
  }

  public CommandContext(Command<?> command, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.command = command;
    this.processEngineConfiguration = processEngineConfiguration;
    this.failedJobCommandFactory = processEngineConfiguration.getFailedJobCommandFactory();
    sessionFactories = processEngineConfiguration.getSessionFactories();
    this.transactionContext = processEngineConfiguration
      .getTransactionContextFactory()
      .openTransactionContext(this);
  }

  public void close() {
    // the intention of this method is that all resources are closed properly, even
    // if exceptions occur in close or flush methods of the sessions or the
    // transaction context.

    try {
      try {
        try {
        	
        	if (exception == null && closeListeners != null) {
	        	try {
	        		for (CommandContextCloseListener listener : closeListeners) {
	        			listener.closing(this);
	        		}
	        	} catch (Throwable exception) {
	        		exception(exception);
	        	}
        	}

          if (exception == null) {
            flushSessions();
          }

        } catch (Throwable exception) {
          exception(exception);
        } finally {
        	
          try {
            if (exception == null) {
              transactionContext.commit();
            }
          } catch (Throwable exception) {
            exception(exception);
          }
          
        	if (exception == null && closeListeners != null) {
	        	try {
	        		for (CommandContextCloseListener listener : closeListeners) {
	        			listener.closed(this);
	        		}
	        	} catch (Throwable exception) {
	        		exception(exception);
	        	}
        	}

          if (exception != null) {
            if (exception instanceof JobNotFoundException || exception instanceof ActivitiTaskAlreadyClaimedException) {
              // reduce log level, because this may have been caused because of job deletion due to cancelActiviti="true"
              log.info("Error while closing command context", exception);
            } else if (exception instanceof ActivitiOptimisticLockingException) {
              // reduce log level, as normally we're not interested in logging this exception
              log.debug("Optimistic locking exception : " + exception);
            } else {
              log.error("Error while closing command context", exception);
            }

            transactionContext.rollback();
          }
        }
      } catch (Throwable exception) {
        exception(exception);
      } finally {
        closeSessions();

      }
    } catch (Throwable exception) {
      exception(exception);
    } 

    // rethrow the original exception if there was one
    if (exception != null) {
      if (exception instanceof Error) {
        throw (Error) exception;
      } else if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new ActivitiException("exception while executing command " + command, exception);
      }
    }
  }
  
  public void addCloseListener(CommandContextCloseListener commandContextCloseListener) {
  	if (closeListeners == null) {
  		closeListeners = new ArrayList<CommandContextCloseListener>(1);
  	}
  	closeListeners.add(commandContextCloseListener);
  }
  
  public List<CommandContextCloseListener> getCloseListeners() {
  	return closeListeners;
  }
 
  protected void flushSessions() {
    for (Session session : sessions.values()) {
      session.flush();
    }
  }

  protected void closeSessions() {
    for (Session session : sessions.values()) {
      try {
        session.close();
      } catch (Throwable exception) {
        exception(exception);
      }
    }
  }

  public void exception(Throwable exception) {
    if (this.exception == null) {
      this.exception = exception;
    } else {
      if (Context.isExecutionContextActive()) {
        LogMDC.putMDCExecution(Context.getExecutionContext().getExecution());
      }
    	log.error("masked exception in command context. for root cause, see below as it will be rethrown later.", exception);    	
    	LogMDC.clear();
    }
  }
  
  public void addAttribute(String key, Object value) {
  	if (attributes == null) {
  		attributes = new HashMap<String, Object>(1);
  	}
  	attributes.put(key, value);
  }
  
  public Object getAttribute(String key) {
  	if (attributes != null) {
  		return attributes.get(key);
  	}
  	return null;
  }

  @SuppressWarnings({"unchecked"})
  public <T> T getSession(Class<T> sessionClass) {
    Session session = sessions.get(sessionClass);
    if (session == null) {
      SessionFactory sessionFactory = sessionFactories.get(sessionClass);
      if (sessionFactory==null) {
        throw new ActivitiException("no session factory configured for "+sessionClass.getName());
      }
      session = sessionFactory.openSession();
      sessions.put(sessionClass, session);
    }

    return (T) session;
  }
  
  public DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }
  
  public DeploymentEntityManager getDeploymentEntityManager() {
    return getSession(DeploymentEntityManager.class);
  }

  public ResourceEntityManager getResourceEntityManager() {
    return getSession(ResourceEntityManager.class);
  }
  
  public ByteArrayEntityManager getByteArrayEntityManager() {
    return getSession(ByteArrayEntityManager.class);
  }
  
  public ProcessDefinitionEntityManager getProcessDefinitionEntityManager() {
    return getSession(ProcessDefinitionEntityManager.class);
  }
  
  public ModelEntityManager getModelEntityManager() {
    return getSession(ModelEntityManager.class);
  }
  
  public ProcessDefinitionInfoEntityManager getProcessDefinitionInfoEntityManager() {
    return getSession(ProcessDefinitionInfoEntityManager.class);
  }

  public ExecutionEntityManager getExecutionEntityManager() {
    return getSession(ExecutionEntityManager.class);
  }

  public TaskEntityManager getTaskEntityManager() {
    return getSession(TaskEntityManager.class);
  }

  public IdentityLinkEntityManager getIdentityLinkEntityManager() {
    return getSession(IdentityLinkEntityManager.class);
  }

  public VariableInstanceEntityManager getVariableInstanceEntityManager() {
    return getSession(VariableInstanceEntityManager.class);
  }

  public HistoricProcessInstanceEntityManager getHistoricProcessInstanceEntityManager() {
    return getSession(HistoricProcessInstanceEntityManager.class);
  }

  public HistoricDetailEntityManager getHistoricDetailEntityManager() {
    return getSession(HistoricDetailEntityManager.class);
  }
  
  public HistoricVariableInstanceEntityManager getHistoricVariableInstanceEntityManager() {
    return getSession(HistoricVariableInstanceEntityManager.class);
  }

  public HistoricActivityInstanceEntityManager getHistoricActivityInstanceEntityManager() {
    return getSession(HistoricActivityInstanceEntityManager.class);
  }
  
  public HistoricTaskInstanceEntityManager getHistoricTaskInstanceEntityManager() {
    return getSession(HistoricTaskInstanceEntityManager.class);
  }
  
  public HistoricIdentityLinkEntityManager getHistoricIdentityLinkEntityManager() {
    return getSession(HistoricIdentityLinkEntityManager.class);
  }
  
  public EventLogEntryEntityManager getEventLogEntryEntityManager() {
  	return getSession(EventLogEntryEntityManager.class);
  }
  
  public JobEntityManager getJobEntityManager() {
    return getSession(JobEntityManager.class);
  }

  public UserIdentityManager getUserIdentityManager() {
    return getSession(UserIdentityManager.class);
  }

  public GroupIdentityManager getGroupIdentityManager() {
    return getSession(GroupIdentityManager.class);
  }

  public IdentityInfoEntityManager getIdentityInfoEntityManager() {
    return getSession(IdentityInfoEntityManager.class);
  }

  public MembershipIdentityManager getMembershipIdentityManager() {
    return getSession(MembershipIdentityManager.class);
  }
  
  public AttachmentEntityManager getAttachmentEntityManager() {
    return getSession(AttachmentEntityManager.class);
  }

  public TableDataManager getTableDataManager() {
    return getSession(TableDataManager.class);
  }

  public CommentEntityManager getCommentEntityManager() {
    return getSession(CommentEntityManager.class);
  }
  
  public PropertyEntityManager getPropertyEntityManager() {
    return getSession(PropertyEntityManager.class);
  }
  
  public EventSubscriptionEntityManager getEventSubscriptionEntityManager() {
    return getSession(EventSubscriptionEntityManager.class);
  }

  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public HistoryManager getHistoryManager() {
    return getSession(HistoryManager.class);
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public TransactionContext getTransactionContext() {
    return transactionContext;
  }
  public Command< ? > getCommand() {
    return command;
  }
  public Map<Class< ? >, Session> getSessions() {
    return sessions;
  }
  public Throwable getException() {
    return exception;
  }
  public FailedJobCommandFactory getFailedJobCommandFactory() {
    return failedJobCommandFactory;
  }
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
	  return processEngineConfiguration;
  }
  public ActivitiEventDispatcher getEventDispatcher() {
  	return processEngineConfiguration.getEventDispatcher();
  }
}
