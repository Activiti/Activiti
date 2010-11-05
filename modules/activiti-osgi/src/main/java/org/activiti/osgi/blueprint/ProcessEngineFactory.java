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

package org.activiti.osgi.blueprint;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandExecutorImpl;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import org.osgi.framework.Bundle;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guillaume Nodet
 */
public class ProcessEngineFactory {

    protected ProcessEngineConfiguration processEngineConfiguration = new ProcessEngineConfiguration();
    protected TransactionManager transactionManager;
    protected ProcessEngineImpl processEngine;
    protected Bundle bundle;

    protected Object jpaEntityManagerFactory;
    protected boolean jpaHandleTransaction = true;
    protected boolean jpaCloseEntityManager = true;


    public void init() throws Exception {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();

        try {
            ClassLoader cl = new BundleDelegatingClassLoader(bundle);

            Thread.currentThread().setContextClassLoader(new ClassLoaderWrapper(
                    cl,
                    ProcessEngineFactory.class.getClassLoader(),
                    ProcessEngineConfiguration.class.getClassLoader(),
                    previous
            ));

            initializeTransactionInterceptor();
            initializeJPA();
            ProcessEngineConfiguration.setCurrentClassLoaderParameter(cl);

            processEngine = (ProcessEngineImpl) processEngineConfiguration.buildProcessEngine();
            ProcessEngines.getProcessEngines().put(processEngine.getName(), processEngine);

            ProcessEngines.registerProcessEngine(processEngine);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    public void destroy() throws Exception {
        if (processEngine != null) {
            ProcessEngines.getProcessEngines().remove(processEngine.getName());
            processEngine.close();
        }
    }

    public ProcessEngine getObject() throws Exception {
        if (processEngine == null) {
            init();
        }
        return processEngine;
    }

    private void initializeTransactionInterceptor() {
        processEngineConfiguration.setTransactionsExternallyManaged(transactionManager != null);

        if (transactionManager != null) {
            List<CommandInterceptor> commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
            commandInterceptorsTxRequired.add(new LogInterceptor());
            commandInterceptorsTxRequired.add(new JtaTransactionInterceptor(transactionManager, false));
            commandInterceptorsTxRequired.add(new CommandContextInterceptor());
            commandInterceptorsTxRequired.add(new CommandExecutorImpl());
            processEngineConfiguration.setCommandInterceptorsTxRequired(commandInterceptorsTxRequired);

            List<CommandInterceptor> commandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
            commandInterceptorsTxRequiresNew.add(new LogInterceptor());
            commandInterceptorsTxRequiresNew.add(new JtaTransactionInterceptor(transactionManager, true));
            commandInterceptorsTxRequiresNew.add(new CommandContextInterceptor());
            commandInterceptorsTxRequiresNew.add(new CommandExecutorImpl());
            processEngineConfiguration.setCommandInterceptorsTxRequiresNew(commandInterceptorsTxRequiresNew);
        }
    }

    private void initializeJPA() {
        if (jpaEntityManagerFactory != null) {
            processEngineConfiguration.enableJPA(jpaEntityManagerFactory, jpaHandleTransaction, jpaCloseEntityManager);
        }
    }

    public Class<?> getObjectType() {
        return ProcessEngine.class;
    }

    public boolean isSingleton() {
        return true;
    }

    // getters and setters //////////////////////////////////////////////////////


    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setDatabaseType(String databaseType) {
        processEngineConfiguration.setDatabaseType(databaseType);
    }

    public void setDataSource(DataSource dataSource) {
        processEngineConfiguration.setDataSource(dataSource);
    }

    public void setDbSchemaStrategy(String dbSchemaStrategy) {
        processEngineConfiguration.setDbSchemaStrategy(dbSchemaStrategy);
    }

    public void setHistoryService(HistoryService historiyService) {
        processEngineConfiguration.setHistoryService(historiyService);
    }

    public void setIdentityService(IdentityService identityService) {
        processEngineConfiguration.setIdentityService(identityService);
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        processEngineConfiguration.setIdGenerator(idGenerator);
    }

    public void setJobExecutor(JobExecutor jobExecutor) {
        processEngineConfiguration.setJobExecutor(jobExecutor);
    }

    public void setJobExecutorActivate(boolean jobExecutorAutoActivate) {
        processEngineConfiguration.setJobExecutorActivate(jobExecutorAutoActivate);
    }

    public void setProcessEngineName(String processEngineName) {
        processEngineConfiguration.setProcessEngineName(processEngineName);
    }

    public void setMailServerHost(String mailServerHost) {
        processEngineConfiguration.setMailServerHost(mailServerHost);
    }

    public void setMailServerPort(int mailServerPort) {
        processEngineConfiguration.setMailServerPort(mailServerPort);
    }

    public void setMailServerUsername(String username) {
        processEngineConfiguration.setMailServerUsername(username);
    }

    public void setMailServerPassword(String password) {
        processEngineConfiguration.setMailServerPassword(password);
    }

    public void setMailServerDefaultFromAddress(String defaultFromAddress) {
        processEngineConfiguration.setMailServerDefaultFrom(defaultFromAddress);
    }

    public void setHistoryLevel(String historyLevelString) {
        processEngineConfiguration.setHistoryLevel(ProcessEngineConfiguration.parseHistoryLevel(historyLevelString));
    }

    public void setJpaEntityManagerFactory(Object jpaEntityManagerFactory) {
        this.jpaEntityManagerFactory = jpaEntityManagerFactory;
    }

    public void setJpaHandleTransaction(boolean jpaHandleTransaction) {
        this.jpaHandleTransaction = jpaHandleTransaction;
    }

    public void setJpaCloseEntityManager(boolean jpaCloseEntityManager) {
        this.jpaCloseEntityManager = jpaCloseEntityManager;
    }
}
