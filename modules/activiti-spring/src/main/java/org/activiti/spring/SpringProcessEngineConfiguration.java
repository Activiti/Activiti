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

package org.activiti.spring;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.spring.autodeployment.AutoDeploymentStrategy;
import org.activiti.spring.autodeployment.DefaultAutoDeploymentStrategy;
import org.activiti.spring.autodeployment.ResourceParentFolderAutoDeploymentStrategy;
import org.activiti.spring.autodeployment.SingleResourceAutoDeploymentStrategy;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Tom Baeyens
 * @author David Syer
 * @author Joram Barrez
 * @author Tiese Barrell
 */
public class SpringProcessEngineConfiguration extends ProcessEngineConfigurationImpl implements ApplicationContextAware {

    protected PlatformTransactionManager transactionManager;
    protected String deploymentName = "SpringAutoDeployment";
    protected Resource[] deploymentResources = new Resource[0];
    protected String deploymentMode = "default";
    protected ApplicationContext applicationContext;
    protected Integer transactionSynchronizationAdapterOrder = null;
    private Collection<AutoDeploymentStrategy> deploymentStrategies = new ArrayList<AutoDeploymentStrategy>();

    public SpringProcessEngineConfiguration() {
        this.transactionsExternallyManaged = true;
        deploymentStrategies.add(new DefaultAutoDeploymentStrategy());
        deploymentStrategies.add(new SingleResourceAutoDeploymentStrategy());
        deploymentStrategies.add(new ResourceParentFolderAutoDeploymentStrategy());
    }

    @Override
    public ProcessEngine buildProcessEngine() {
        ProcessEngine processEngine = super.buildProcessEngine();
        ProcessEngines.setInitialized(true);
        autoDeployResources(processEngine);
        return processEngine;
    }

    public void setTransactionSynchronizationAdapterOrder(Integer transactionSynchronizationAdapterOrder) {
        this.transactionSynchronizationAdapterOrder = transactionSynchronizationAdapterOrder;
    }

    @Override
    protected void initDefaultCommandConfig() {
        if (defaultCommandConfig == null) {
            defaultCommandConfig = new CommandConfig().setContextReusePossible(true);
        }
    }

    @Override
    protected CommandInterceptor createTransactionInterceptor() {
        if (transactionManager == null) {
            throw new ActivitiException("transactionManager is required property for SpringProcessEngineConfiguration, use "
                    + StandaloneProcessEngineConfiguration.class.getName() + " otherwise");
        }

        return new SpringTransactionInterceptor(transactionManager);
    }

    @Override
    protected void initTransactionContextFactory() {
        if (transactionContextFactory == null && transactionManager != null) {
            transactionContextFactory = new SpringTransactionContextFactory(transactionManager, transactionSynchronizationAdapterOrder);
        }
    }

    @Override
    protected void initJpa() {
        super.initJpa();
        if (jpaEntityManagerFactory != null) {
            sessionFactories.put(EntityManagerSession.class, new SpringEntityManagerSessionFactory(jpaEntityManagerFactory, jpaHandleTransaction,
                    jpaCloseEntityManager));
        }
    }

    protected void autoDeployResources(ProcessEngine processEngine) {
        if (deploymentResources != null && deploymentResources.length > 0) {
            final AutoDeploymentStrategy strategy = getAutoDeploymentStrategy(deploymentMode);
            strategy.deployResources(deploymentName, deploymentResources, processEngine.getRepositoryService());
        }
    }

    @Override
    public ProcessEngineConfiguration setDataSource(DataSource dataSource) {
        if (dataSource instanceof TransactionAwareDataSourceProxy) {
            return super.setDataSource(dataSource);
        } else {
            // Wrap datasource in Transaction-aware proxy
            DataSource proxiedDataSource = new TransactionAwareDataSourceProxy(dataSource);
            return super.setDataSource(proxiedDataSource);
        }
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public Resource[] getDeploymentResources() {
        return deploymentResources;
    }

    public void setDeploymentResources(Resource[] deploymentResources) {
        this.deploymentResources = deploymentResources;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getDeploymentMode() {
        return deploymentMode;
    }

    public void setDeploymentMode(String deploymentMode) {
        this.deploymentMode = deploymentMode;
    }

    /**
     * Gets the {@link AutoDeploymentStrategy} for the provided mode. This method
     * may be overridden to implement custom deployment strategies if required,
     * but implementors should take care not to return <code>null</code>.
     *
     * @param mode the mode to get the strategy for
     * @return the deployment strategy to use for the mode. Never
     * <code>null</code>
     */
    protected AutoDeploymentStrategy getAutoDeploymentStrategy(final String mode) {
        AutoDeploymentStrategy result = new DefaultAutoDeploymentStrategy();
        for (final AutoDeploymentStrategy strategy : deploymentStrategies) {
            if (strategy.handlesMode(mode)) {
                result = strategy;
                break;
            }
        }
        return result;
    }

}
