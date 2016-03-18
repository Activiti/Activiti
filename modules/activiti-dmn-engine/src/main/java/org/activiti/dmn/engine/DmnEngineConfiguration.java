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
package org.activiti.dmn.engine;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.activiti.dmn.engine.domain.repository.DmnRepositoryManager;
import org.activiti.dmn.engine.impl.DmnEngineImpl;
import org.activiti.dmn.engine.impl.DmnRepositoryServiceImpl;
import org.activiti.dmn.engine.impl.DmnRuleServiceImpl;
import org.activiti.dmn.engine.impl.ServiceImpl;
import org.activiti.dmn.engine.impl.cfg.DmnPersistenceUnitInfo;
import org.activiti.dmn.engine.impl.cfg.StandaloneDmnEngineConfiguration;
import org.activiti.dmn.engine.impl.cfg.StandaloneInMemDmnEngineConfiguration;
import org.activiti.dmn.engine.impl.deploy.DecisionTableCacheEntry;
import org.activiti.dmn.engine.impl.deploy.DefaultDeploymentCache;
import org.activiti.dmn.engine.impl.deploy.DeploymentCache;
import org.activiti.dmn.engine.impl.deploy.DeploymentCacheManager;
import org.activiti.dmn.engine.impl.deployer.DmnDeployer;
import org.activiti.dmn.engine.impl.mvel.config.DefaultCustomExpressionFunctionRegistry;
import org.activiti.dmn.engine.impl.parser.DmnParseFactory;
import org.hibernate.ejb.HibernatePersistence;
import org.mvel2.integration.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public abstract class DmnEngineConfiguration {
    
    protected static final Logger logger = LoggerFactory.getLogger(DmnEngineConfiguration.class);

    /** The tenant id indicating 'no tenant' */
    public static final String NO_TENANT_ID = "";

    protected String dmnEngineName = DmnEngines.NAME_DEFAULT;

    protected String databaseType;
    protected String jdbcDriver = "org.h2.Driver";
    protected String jdbcUrl = "jdbc:h2:tcp://localhost/~/activitidmn";
    protected String jdbcUsername = "sa";
    protected String jdbcPassword = "";
    protected String dataSourceJndiName;
    protected int jdbcMinPoolSize = 10;
    protected int jdbcMaxPoolSize = 100;
    protected int jdbcMaxIdleTime = 1800;
    protected int jdbcMaxIdleTimeExcessConnections = 1800;
    protected String jdbcPingQuery;
    protected DataSource dataSource;

    protected String xmlEncoding = "UTF-8";

    protected BeanFactory beanFactory;
    
    // SERVICES
    // /////////////////////////////////////////////////////////////////

    protected DmnRepositoryServiceImpl repositoryService = new DmnRepositoryServiceImpl();
    protected DmnRuleServiceImpl ruleService = new DmnRuleServiceImpl();

    // DEPLOYERS
    // ////////////////////////////////////////////////////////////////

    protected DmnDeployer dmnDeployer;
    protected DmnParseFactory dmnParseFactory;

    protected int decisionCacheLimit = -1; // By default, no limit
    protected DeploymentCache<DecisionTableCacheEntry> decisionCache;

    protected EntityManagerFactory entityManagerFactory;
    
    protected DeploymentCacheManager deploymentCacheManager;
    protected DmnRepositoryManager dmnRepositoryManager;

    // CUSTOM EXPRESSION FUNCTIONS
    // ////////////////////////////////////////////////////////////////
    protected CustomExpressionFunctionRegistry customExpressionFunctionRegistry;
    protected CustomExpressionFunctionRegistry postCustomExpressionFunctionRegistry;
    protected Map<String, Method> customExpressionFunctions = new HashMap<String, Method>();
    protected Map<Class<?>, PropertyHandler> customPropertyHandlers = new HashMap<Class<?>, PropertyHandler>();

    /**
     * Set this to true if you want to have extra checks on the BPMN xml that is
     * parsed. See
     * http://www.jorambarrez.be/blog/2013/02/19/uploading-a-funny-xml
     * -can-bring-down-your-server/
     * 
     * Unfortunately, this feature is not available on some platforms (JDK 6,
     * JBoss), hence the reason why it is disabled by default. If your platform
     * allows the use of StaxSource during XML parsing, do enable it.
     */
    protected boolean enableSafeDmnXml;

    /** use one of the static createXxxx methods instead */
    protected DmnEngineConfiguration() {
    }

    public static DmnEngineConfiguration createDmnEngineConfigurationFromResourceDefault() {
        return createDmnEngineConfigurationFromResource("activiti.dmn.cfg.xml", "dmnEngineConfiguration");
    }

    public static DmnEngineConfiguration createDmnEngineConfigurationFromResource(String resource) {
        return createDmnEngineConfigurationFromResource(resource, "dmnEngineConfiguration");
    }

    public static DmnEngineConfiguration createDmnEngineConfigurationFromResource(String resource, String beanName) {
        return parseProcessEngineConfigurationFromResource(resource, beanName);
    }

    public static DmnEngineConfiguration createDmnEngineConfigurationFromInputStream(InputStream inputStream) {
        return createDmnEngineConfigurationFromInputStream(inputStream, "dmnEngineConfiguration");
    }

    public static DmnEngineConfiguration createDmnEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
        return parseProcessEngineConfigurationFromInputStream(inputStream, beanName);
    }

    public static DmnEngineConfiguration createStandaloneDmnEngineConfiguration() {
        return new StandaloneDmnEngineConfiguration();
    }

    public static DmnEngineConfiguration createStandaloneInMemDmnEngineConfiguration() {
        return new StandaloneInMemDmnEngineConfiguration();
    }

    public static DmnEngineConfiguration parseDmnEngineConfiguration(Resource springResource, String beanName) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        xmlBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        xmlBeanDefinitionReader.loadBeanDefinitions(springResource);
        DmnEngineConfiguration processEngineConfiguration = (DmnEngineConfiguration) beanFactory.getBean(beanName);
        processEngineConfiguration.setBeanFactory(beanFactory);
        return processEngineConfiguration;
    }

    public static DmnEngineConfiguration parseProcessEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
        Resource springResource = new InputStreamResource(inputStream);
        return parseDmnEngineConfiguration(springResource, beanName);
    }

    public static DmnEngineConfiguration parseProcessEngineConfigurationFromResource(String resource, String beanName) {
        Resource springResource = new ClassPathResource(resource);
        return parseDmnEngineConfiguration(springResource, beanName);
    }

    // buildProcessEngine
    // ///////////////////////////////////////////////////////

    public DmnEngine buildDmnEngine() {
        init();
        return new DmnEngineImpl(this);
    }

    // init
    // /////////////////////////////////////////////////////////////////////

    protected void init() {
        initDataSource();
        initEntityManagerFactory();
        initRepositoryManager();
        initServices();
        initDeployers();
        initCustomExpressionFunctions();
    }
    
    // services
    // /////////////////////////////////////////////////////////////////

    protected void initRepositoryManager() {
        dmnRepositoryManager = new DmnRepositoryManager(this);
    }
    
    protected void initServices() {
        initService(repositoryService);
        initService(ruleService);
    }

    protected void initService(ServiceImpl service) {
        service.setDmnEngineConfiguration(this);
    }

    // DataSource
    // ///////////////////////////////////////////////////////////////

    protected void initDataSource() {
        if (dataSource == null) {
            if (dataSourceJndiName != null) {
                try {
                    dataSource = (DataSource) new InitialContext().lookup(dataSourceJndiName);
                } catch (Exception e) {
                    throw new ActivitiDmnException("couldn't lookup datasource from " + dataSourceJndiName + ": " + e.getMessage(), e);
                }

            } else if (jdbcUrl != null) {
                if ((jdbcDriver == null) || (jdbcUsername == null)) {
                    throw new ActivitiDmnException("DataSource or JDBC properties have to be specified in a process engine configuration");
                }

                logger.debug("initializing datasource to db: {}", jdbcUrl);

                if (logger.isInfoEnabled()) {
                    logger.info("Configuring Datasource with following properties (omitted password for security)");
                    logger.info("datasource driver: " + jdbcDriver);
                    logger.info("datasource url : " + jdbcUrl);
                    logger.info("datasource user name : " + jdbcUsername);
                    logger.info("Min pool size | Max pool size | acquire increment : " + jdbcMinPoolSize + " | " + jdbcMaxPoolSize);
                }
                
                ComboPooledDataSource ds = new ComboPooledDataSource();
                try {
                    ds.setDriverClass(jdbcDriver);
                } catch (PropertyVetoException e) {
                    logger.error("Could not set Jdbc Driver class", e);
                    return;
                }
                    
                // Connection settings
                ds.setJdbcUrl(jdbcUrl);
                ds.setUser(jdbcUsername);
                ds.setPassword(jdbcPassword);
                
                // Pool config: see http://www.mchange.com/projects/c3p0/#configuration
                ds.setMinPoolSize(jdbcMinPoolSize);
                ds.setMaxPoolSize(jdbcMaxPoolSize);
                if (jdbcPingQuery != null) {
                    ds.setPreferredTestQuery(jdbcPingQuery);
                }
                ds.setMaxIdleTimeExcessConnections(jdbcMaxIdleTimeExcessConnections);
                ds.setMaxIdleTime(jdbcMaxIdleTime);
                
                this.dataSource = ds;
            }
        }
    }

    public void initEntityManagerFactory() {
        DmnPersistenceUnitInfo persistenceInfo = new DmnPersistenceUnitInfo();
        persistenceInfo.setPersistenceProviderClassName("org.hibernate.ejb.HibernatePersistence");
        persistenceInfo.setPersistenceUnitName("activiti-dmn-pu");
        persistenceInfo.setNonJtaDataSource(dataSource);
        persistenceInfo.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
        String baseEntityPackage = "org.activiti.dmn.engine.domain.entity.";
        persistenceInfo.addManagedClassName(baseEntityPackage + "DmnDeployment");
        persistenceInfo.addManagedClassName(baseEntityPackage + "DmnDeploymentResource");
        persistenceInfo.addManagedClassName(baseEntityPackage + "DmnDecisionTable");
        persistenceInfo.setExcludeUnlistedClasses(true);

        Map<String, Object> propertiesMap = new HashMap<String, Object>();
        propertiesMap.put("hibernate.show_sql", false);
        propertiesMap.put("hibernate.cache.use_second_level_cache", false);
        propertiesMap.put("hibernate.generate_statistics", false);

        HibernatePersistence persistenceProvider = new HibernatePersistence();
        this.entityManagerFactory = persistenceProvider.createContainerEntityManagerFactory(persistenceInfo, propertiesMap);
    }

    // deployers
    // ////////////////////////////////////////////////////////////////

    protected void initDeployers() {
        if (dmnParseFactory == null) {
            dmnParseFactory = new DmnParseFactory();
        }
        
        if (this.dmnDeployer == null) {
            this.dmnDeployer = new DmnDeployer(dmnParseFactory, this);
        }
        
        // Decision cache
        if (decisionCache == null) {
          if (decisionCacheLimit <= 0) {
              decisionCache = new DefaultDeploymentCache<DecisionTableCacheEntry>();
          } else {
              decisionCache = new DefaultDeploymentCache<DecisionTableCacheEntry>(decisionCacheLimit);
          }
        }
        
        deploymentCacheManager = new DeploymentCacheManager(decisionCache, this);
    }

    // custom expression functions
    // ////////////////////////////////////////////////////////////////
    protected void initCustomExpressionFunctions() {
        if (customExpressionFunctionRegistry == null) {
            customExpressionFunctions.putAll(new DefaultCustomExpressionFunctionRegistry().getCustomExpressionMethods());
        } else {
            customExpressionFunctions.putAll(customExpressionFunctionRegistry.getCustomExpressionMethods());
        }

        if (postCustomExpressionFunctionRegistry != null) {
            customExpressionFunctions.putAll(postCustomExpressionFunctionRegistry.getCustomExpressionMethods());
        }
    }

    // getters and setters
    // //////////////////////////////////////////////////////

    public String getDmnEngineName() {
        return dmnEngineName;
    }

    public DmnEngineConfiguration setDmnEngineName(String dmnEngineName) {
        this.dmnEngineName = dmnEngineName;
        return this;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public DmnEngineConfiguration setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public DmnEngineConfiguration setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public DmnEngineConfiguration setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
        return this;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public DmnEngineConfiguration setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public DmnEngineConfiguration setJdbcUsername(String jdbcUsername) {
        this.jdbcUsername = jdbcUsername;
        return this;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public DmnEngineConfiguration setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
        return this;
    }

    public int getJdbcMinPoolSize() {
        return jdbcMinPoolSize;
    }

    public DmnEngineConfiguration setJdbcMinPoolSize(int jdbcMinPoolSize) {
        this.jdbcMinPoolSize = jdbcMinPoolSize;
        return this;
    }

    public int getJdbcMaxPoolSize() {
        return jdbcMaxPoolSize;
    }

    public DmnEngineConfiguration setJdbcMaxPoolSize(int jdbcMaxPoolSize) {
        this.jdbcMaxPoolSize = jdbcMaxPoolSize;
        return this;
    }

    public int getJdbcMaxIdleTime() {
        return jdbcMaxIdleTime;
    }

    public DmnEngineConfiguration setJdbcMaxIdleTime(int jdbcMaxIdleTime) {
        this.jdbcMaxIdleTime = jdbcMaxIdleTime;
        return this;
    }

    public int getJdbcMaxIdleTimeExcessConnections() {
        return jdbcMaxIdleTimeExcessConnections;
    }

    public DmnEngineConfiguration setJdbcMaxIdleTimeExcessConnections(int jdbcMaxIdleTimeExcessConnections) {
        this.jdbcMaxIdleTimeExcessConnections = jdbcMaxIdleTimeExcessConnections;
        return this;
    }

    public String getJdbcPingQuery() {
        return jdbcPingQuery;
    }

    public DmnEngineConfiguration setJdbcPingQuery(String jdbcPingQuery) {
        this.jdbcPingQuery = jdbcPingQuery;
        return this;
    }

    public String getDataSourceJndiName() {
        return dataSourceJndiName;
    }

    public DmnEngineConfiguration setDataSourceJndiName(String dataSourceJndiName) {
        this.dataSourceJndiName = dataSourceJndiName;
        return this;
    }

    public String getXmlEncoding() {
        return xmlEncoding;
    }

    public DmnEngineConfiguration setXmlEncoding(String xmlEncoding) {
        this.xmlEncoding = xmlEncoding;
        return this;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public DmnEngineConfiguration setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        return this;
    }
    
    public DmnRepositoryServiceImpl getDmnRepositoryService() {
        return repositoryService;
    }

    public DmnRuleServiceImpl getDmnRuleService() {
        return ruleService;
    }

    public DeploymentCacheManager getDeploymentCacheManager() {
        return deploymentCacheManager;
    }

    public DmnRepositoryManager getDmnRepositoryManager() {
        return dmnRepositoryManager;
    }

    public DmnEngineConfiguration getDmnEngineConfiguration() {
        return this;
    }

    public DmnDeployer getDmnDeployer() {
        return dmnDeployer;
    }

    public DmnEngineConfiguration setDmnDeployer(DmnDeployer dmnDeployer) {
        this.dmnDeployer = dmnDeployer;
        return this;
    }

    public DmnParseFactory getDmnParseFactory() {
        return dmnParseFactory;
    }

    public DmnEngineConfiguration setDmnParseFactory(DmnParseFactory dmnParseFactory) {
        this.dmnParseFactory = dmnParseFactory;
        return this;
    }

    public int getDecisionCacheLimit() {
        return decisionCacheLimit;
    }

    public DmnEngineConfiguration setDecisionCacheLimit(int decisionCacheLimit) {
        this.decisionCacheLimit = decisionCacheLimit;
        return this;
    }

    public DeploymentCache<DecisionTableCacheEntry> getDecisionCache() {
        return decisionCache;
    }

    public DmnEngineConfiguration setDecisionCache(DeploymentCache<DecisionTableCacheEntry> decisionCache) {
        this.decisionCache = decisionCache;
        return this;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public DmnEngineConfiguration setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        return this;
    }

    public boolean isEnableSafeDmnXml() {
        return enableSafeDmnXml;
    }

    public DmnEngineConfiguration setEnableSafeDmnXml(boolean enableSafeDmnXml) {
        this.enableSafeDmnXml = enableSafeDmnXml;
        return this;
    }

    public CustomExpressionFunctionRegistry getCustomExpressionFunctionRegistry() {
        return customExpressionFunctionRegistry;
    }

    public void setCustomExpressionFunctionRegistry(CustomExpressionFunctionRegistry customExpressionFunctionRegistry) {
        this.customExpressionFunctionRegistry = customExpressionFunctionRegistry;
    }

    public CustomExpressionFunctionRegistry getPostCustomExpressionFunctionRegistry() {
        return postCustomExpressionFunctionRegistry;
    }

    public void setPostCustomExpressionFunctionRegistry(CustomExpressionFunctionRegistry postCustomExpressionFunctionRegistry) {
        this.postCustomExpressionFunctionRegistry = postCustomExpressionFunctionRegistry;
    }

    public Map<String, Method> getCustomExpressionFunctions() {
        return customExpressionFunctions;
    }

    public void setCustomExpressionFunctions(Map<String, Method> customExpressionFunctions) {
        this.customExpressionFunctions = customExpressionFunctions;
    }
    
    public Map<Class<?>, PropertyHandler> getCustomPropertyHandlers() {
        return customPropertyHandlers;
    }

    public void setCustomPropertyHandlers(Map<Class<?>, PropertyHandler> customPropertyHandlers) {
        this.customPropertyHandlers = customPropertyHandlers;
    }
}
