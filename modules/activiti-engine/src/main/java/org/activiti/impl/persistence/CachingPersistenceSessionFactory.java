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

package org.activiti.impl.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.activiti.ActivitiException;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.repository.DeploymentImpl;

/**
 * @author Dave Syer
 */
public class CachingPersistenceSessionFactory implements PersistenceSessionFactory {

  private Map<String, ProcessDefinitionImpl> processDefinitionsById = new HashMap<String, ProcessDefinitionImpl>();
  private Map<String, ProcessDefinitionImpl> processDefinitionsByKey = new HashMap<String, ProcessDefinitionImpl>();

  private final PersistenceSessionFactory delegate;
  private final ClassLoader classLoader;
  private final DeployerManager deployerManager;

  public CachingPersistenceSessionFactory(PersistenceSessionFactory delegate, DeployerManager deployerManager, ClassLoader classLoader) {
    this.delegate = delegate;
    this.deployerManager = deployerManager;
    this.classLoader = classLoader;
  }

  public void dbSchemaCheckVersion() {
    delegate.dbSchemaCheckVersion();
  }

  public void dbSchemaCreate() {
    delegate.dbSchemaCreate();
  }

  public void dbSchemaDrop() {
    delegate.dbSchemaDrop();
  }

  public PersistenceSession openPersistenceSession(CommandContext commandContext) {
    return (PersistenceSession) Proxy.newProxyInstance(classLoader, new Class< ? >[] { PersistenceSession.class }, new CacheHandler(delegate
            .openPersistenceSession(commandContext)));
  }

  public synchronized void reset() {
    processDefinitionsById = new HashMap<String, ProcessDefinitionImpl>();
    processDefinitionsByKey = new HashMap<String, ProcessDefinitionImpl>();
  }

  private synchronized void add(ProcessDefinitionImpl processDefinition) {
    processDefinitionsById.put(processDefinition.getId(), processDefinition);
    processDefinitionsByKey.put(processDefinition.getKey(), processDefinition);
  }

  private void addProcessDefinition(PersistenceSession persistenceSession, ProcessDefinitionImpl processDefinition) {

    if (processDefinition == null) {
      return;
    }

    if (processDefinition.isNew()) {
      persistenceSession.insertProcessDefinition(processDefinition);
    } else {
      String deploymentId = processDefinition.getDeployment().getId();
      ProcessDefinitionImpl persistedProcessDefinition = persistenceSession.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
      processDefinition.setId(persistedProcessDefinition.getId());
      processDefinition.setVersion(persistedProcessDefinition.getVersion());
    }

    add(processDefinition);

  }

  private synchronized ProcessDefinitionImpl findProcessDefinitionById(PersistenceSession persistenceSession, String processDefinitionId) {

    ProcessDefinitionImpl processDefinition = processDefinitionsById.get(processDefinitionId);
    if (processDefinition == null) {
      addProcessDefinition(persistenceSession, persistenceSession.findProcessDefinitionById(processDefinitionId));
      DeploymentImpl deployment = persistenceSession.findDeploymentByProcessDefinitionId(processDefinitionId);
      if (deployment != null) {
        // FIXME: remove command context if possible
        deployerManager.deploy(deployment, CommandContext.getCurrent());
      }
    }

    processDefinition = processDefinitionsById.get(processDefinitionId);
    if (processDefinition != null) {
      return processDefinition;
    } else {
      throw new ActivitiException("Couldn't find process definiton with id " + processDefinitionId);
    }
  }

  private ProcessDefinitionImpl findProcessDefinitionByKey(PersistenceSession persistenceSession, String processDefinitionKey) {
    ProcessDefinitionImpl processDefinition = persistenceSession.findLatestProcessDefinitionByKey(processDefinitionKey);
    if (processDefinition == null) {
      return null;
    }
    return findProcessDefinitionById(persistenceSession, processDefinition.getId());
  }

  private class CacheHandler implements InvocationHandler {

    private final PersistenceSession persistenceSession;

    public CacheHandler(PersistenceSession persistenceSession) {
      this.persistenceSession = persistenceSession;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      if ("findLatestProcessDefinitionByKey".equals(methodName)) {
        return findProcessDefinitionByKey(persistenceSession, (String) args[0]);
      } else if ("findProcessDefinitionById".equals(methodName)) {
        return findProcessDefinitionById(persistenceSession, (String) args[0]);
      } else if ("insertProcessDefinition".equals(methodName)) {
        ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) args[0];
        addProcessDefinition(persistenceSession, processDefinition);
        return null;
      }
      return method.invoke(persistenceSession, args);
    }

  }

  public PersistenceSessionFactory getTargetPersistenceSessionFactory() {
    return delegate;
  }
}