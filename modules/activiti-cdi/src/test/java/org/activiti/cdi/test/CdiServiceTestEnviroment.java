package org.activiti.cdi.test;

import java.util.Properties;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;

import org.activiti.cdi.SeamConfiguredProcessEngine;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;

/**
 * Sets up the environment for cycle service tests.
 *  
 * @author Daniel Meyer
 * 
 */
public class CdiServiceTestEnviroment extends CdiTestEnvironment {
  
  private static Logger log = Logger.getLogger(CdiServiceTestEnviroment.class.getName());

  protected String datasourcePropertiesFileName = "datasource.properties";

  protected SimpleNamingContextBuilder jndiBuilder;

  private AtomikosNonXADataSourceBean cycleDatasource;
  private AtomikosNonXADataSourceBean activitiDatasource;

  protected UserTransactionManager transactionManager;

  @Override
  public void setup() throws Exception {
    setupJndi();
    setUpTransactionManager();
    setupDatasources();  
    setupCdi();    
    
    bootPersistenceUnit();
  }

  @Override
  public void teardown() throws Exception {
    super.teardown();
    closeTransactionManager();
    closeDatasources();
  }


  protected void setupDatasources() throws Exception {
    setUpCycleDatasource();
    setUpActivitiDatasource();
  }

  protected void setupJndi() throws Exception {
    log.info("=========== setting up JNDI");
    jndiBuilder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
  }

  protected void setUpCycleDatasource() {
    log.info("=========== setting up datasource java:comp/env/jdbc/activiti-cycle");
    // create the datasource for cycle and activiti
    AtomikosNonXADataSourceBean cycleDatasource = new AtomikosNonXADataSourceBean();

    Properties datasourceProperties = CdiTestUtils.loadProperties(datasourcePropertiesFileName);
    cycleDatasource.setDriverClassName(datasourceProperties.getProperty("ds.driverClassName"));
    cycleDatasource.setUrl(datasourceProperties.getProperty("ds.url"));
    cycleDatasource.setUser(datasourceProperties.getProperty("ds.user"));
    cycleDatasource.setPassword(datasourceProperties.getProperty("ds.password"));
    cycleDatasource.setUniqueResourceName("cycle-ds");
    cycleDatasource.setMaxPoolSize(2);
        
    this.cycleDatasource = cycleDatasource;

    // bind datasource in jndi
    jndiBuilder.bind("java:comp/env/jdbc/activiti-cycle", cycleDatasource);
  }

  protected void setUpActivitiDatasource() {
    log.info("=========== setting up datasource java:comp/env/jdbc/activiti-engine");
    // in the default configuration we use the same datasource for cycle and
    // activiti we bind the cycle datasource under a different name:
    activitiDatasource = cycleDatasource;
    jndiBuilder.bind("java:comp/env/jdbc/activiti-engine", activitiDatasource);
  }

  protected void setUpTransactionManager() throws Exception {
    log.info("=========== setting up JTA transaction manager");
    transactionManager = new UserTransactionManager();
    transactionManager.init();

    jndiBuilder.bind("java:comp/env/TransactionManager", transactionManager);
    jndiBuilder.bind("java:comp/UserTransaction", new UserTransactionImp());
  }
  
  protected void bootPersistenceUnit() {
    log.info("=========== booting JPA persistence unit");
    ProgrammaticBeanLookup.lookup(EntityManagerFactory.class).createEntityManager().close();
  }
    
  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void cleanActivitiTables() {
    log.info("=========== dropping / creating the activiti database schema");
    CommandExecutor commandExecutor = ProgrammaticBeanLookup.lookup(SeamConfiguredProcessEngine.class)
            .getProcessEngineConfiguration()
            .getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        DbSqlSession session = commandContext.getSession(DbSqlSession.class);
        session.dbSchemaDrop();
        session.dbSchemaCreate();
        return null;
      }
    });
  }

  protected void closeDatasources() {
    closeActivitiDatasource();
    closeCycleDatasource();
  }
  
  protected void closeActivitiDatasource() {
    // no-op
  }
  
  protected void closeCycleDatasource() {
    log.info("=========== closing the datasource");
    cycleDatasource.close();
  }
  
  protected void closeTransactionManager() {
    log.info("=========== closing the transaction manager");
    transactionManager.close();
  }

}
