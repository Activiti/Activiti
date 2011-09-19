package org.activiti.cdi.test;

import java.util.Properties;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;

/**
 * Environment setting up different XA-datasources for activiti and cycle
 * 
 * Also handles activiti schema management using a non-xa datasource: Some
 * SQL-Statements like CREATE or ALTER TABLE force an implicit commit, which is
 * why they are disallowed in a global xa-transaciton.
 * 
 * NOTE: OpenJPA can be configured using a second non-xa datasource (cf.
 * persistence.xml)
 * 
 * @author Daniel Meyer
 */
public class CdiXaTestEnvironment extends CdiServiceTestEnviroment {
  
  private static Logger log = Logger.getLogger(CdiXaTestEnvironment.class.getName());

  protected String engineDsPropertiesFileName = "engine-ds-xa.properties";
  protected String cycleDsPropertiesFileName = "cycle-ds-xa.properties";
  protected AtomikosNonXADataSourceBean nonXaActivitiDs;
  protected ProcessEngineConfigurationImpl nonXaProcessEngineConfiguration;
  
  private AtomikosDataSourceBean activitiDatasource;
  private AtomikosDataSourceBean cycleDatasource;

  @Override
  public void setup() throws Exception {
    setUpNonXaActivitiDs();
    setupNonXaProcessEngineConfiguration();
    createActivitiSchemaUsingNonXaConnection();
    super.setup();
  }
  
  @Override
  public void teardown() throws Exception {
    super.teardown();
    dropActivitiSchemaUsingNonXaConnection();
    closeNonXaActivitiDs();
  }

  protected void setUpActivitiDatasource() {
    log.info("=========== setting up datasource java:comp/env/jdbc/activiti-engine");
    Properties datasourceProperties = CdiTestUtils.loadProperties(datasourcePropertiesFileName);
    // create the datasource for activiti engine
    AtomikosDataSourceBean engineDs = new AtomikosDataSourceBean();
    engineDs.setXaDataSourceClassName(datasourceProperties.getProperty("ds.engine.xaDriverClassName"));
    engineDs.setXaProperties(CdiTestUtils.loadProperties(engineDsPropertiesFileName));
    engineDs.setUniqueResourceName("activiti-engine");
    engineDs.setMaxPoolSize(2);
    
    activitiDatasource = engineDs;

    jndiBuilder.bind("java:comp/env/jdbc/activiti-engine", activitiDatasource);
  }

  protected void setUpCycleDatasource() {
    log.info("=========== setting up datasource java:comp/env/jdbc/activiti-cycle");
    Properties datasourceProperties = CdiTestUtils.loadProperties(datasourcePropertiesFileName);
    // create the datasource for activiti cycle
    AtomikosDataSourceBean cycleDs = new AtomikosDataSourceBean();
    cycleDs.setXaDataSourceClassName(datasourceProperties.getProperty("ds.cycle.xaDriverClassName"));
    cycleDs.setXaProperties(CdiTestUtils.loadProperties(cycleDsPropertiesFileName));
    cycleDs.setUniqueResourceName("activiti-cycle");
    cycleDs.setMaxPoolSize(2);
    
    cycleDatasource = cycleDs;

    jndiBuilder.bind("java:comp/env/jdbc/activiti-cycle", cycleDatasource);
  }
  
  @Override
  protected void closeActivitiDatasource() {
    log.info("=========== closing datasource java:comp/env/jdbc/activiti-engine");
    activitiDatasource.close();
  }
  
  @Override
  protected void closeCycleDatasource() {
    log.info("=========== closing datasource java:comp/env/jdbc/activiti-cycle");
    cycleDatasource.close();
  }

  protected void createActivitiSchemaUsingNonXaConnection() {
    log.info("=========== creating activiti db schema");
    nonXaProcessEngineConfiguration
      .buildProcessEngine()
      .close();
  }

  protected void dropActivitiSchemaUsingNonXaConnection() {
    log.info("=========== dropping activiti db schema");
    nonXaProcessEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        commandContext.getDbSqlSession().dbSchemaDrop();
        return null;
      }
    });
  }

  protected void setupNonXaProcessEngineConfiguration() {
    Properties datasourceProperties = CdiTestUtils.loadProperties(datasourcePropertiesFileName);
    nonXaProcessEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
            .createStandaloneProcessEngineConfiguration()
            .setDataSource(nonXaActivitiDs)
            .setDatabaseType(datasourceProperties.getProperty("ds.engine.databaseType"))
            .setDatabaseSchemaUpdate("true");
  }

  protected void setUpNonXaActivitiDs() {
    Properties datasourceProperties = CdiTestUtils.loadProperties(datasourcePropertiesFileName);
    AtomikosNonXADataSourceBean engineDs = new AtomikosNonXADataSourceBean();
    engineDs.setDriverClassName(datasourceProperties.getProperty("ds.engine.driverClassName"));
    engineDs.setUrl(datasourceProperties.getProperty("ds.engine.url"));
    engineDs.setUser(datasourceProperties.getProperty("ds.engine.user"));
    engineDs.setPassword(datasourceProperties.getProperty("ds.engine.password"));
    engineDs.setUniqueResourceName("activiti-engine2");
    engineDs.setMaxPoolSize(2);
    nonXaActivitiDs = engineDs;
  }
  
  protected void closeNonXaActivitiDs() {
    nonXaActivitiDs.close();
  }
  
  @Override
  public void cleanActivitiTables() {
    log.info("=========== dropping / creating the activiti database schema");

    CommandExecutor commandExecutor = nonXaProcessEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        DbSqlSession session = commandContext.getSession(DbSqlSession.class);
        session.dbSchemaDrop();
        session.dbSchemaCreate();
        return null;
      }
    });

  }
}
