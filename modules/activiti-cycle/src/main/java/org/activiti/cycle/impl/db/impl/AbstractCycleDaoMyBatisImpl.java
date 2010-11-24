package org.activiti.cycle.impl.db.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.activiti.cycle.impl.conf.CycleDbSqlSessionFactory;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;


public abstract class AbstractCycleDaoMyBatisImpl {
  
  private static HashMap<String, CycleDbSqlSessionFactory> dbFactories = new HashMap<String, CycleDbSqlSessionFactory>();
  protected String processEngineName = DEFAULT_ENGINE;
  protected static String DEFAULT_ENGINE = "DEFAULT_PROCESS_ENGINE";
  
  private static Logger log = Logger.getLogger(AbstractCycleDaoMyBatisImpl.class.getName());
  
  protected SqlSessionFactory getSessionFactory() {
    if (dbFactories.get(processEngineName) == null) {
      synchronized (dbFactories) {
        // lazy initialization, only done once per proces engine!
        if (dbFactories.get(processEngineName) == null) {
          ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) getProcessEngineConfiguration();
          
          DataSource dataSource = processEngineConfiguration.getDataSource();
          TransactionFactory transactionFactory = processEngineConfiguration.getTransactionFactory();
          SqlSessionFactory sqlSessionFactory = createSessionFactory(dataSource, transactionFactory);
          
          CycleDbSqlSessionFactory factory = new CycleDbSqlSessionFactory();
          factory.setDatabaseType(processEngineConfiguration.getDatabaseType());
          factory.setIdGenerator(processEngineConfiguration.getIdGenerator());
          factory.setSqlSessionFactory(sqlSessionFactory);
          
          performDbSchemaCreation(factory, processEngineConfiguration);
          dbFactories.put(processEngineName, factory);
        }
      }
    }
    return dbFactories.get(processEngineName).getSqlSessionFactory();
  }
  
  public SqlSessionFactory createSessionFactory(DataSource dataSource, TransactionFactory transactionFactory) {
    InputStream inputStream = null;
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      inputStream = classLoader.getResourceAsStream("org/activiti/db/cycle/ibatis/activiti.ibatis.mem.conf.xml");

      // update the jdbc parameters to the configured ones...
      Environment environment = new Environment("default", transactionFactory, dataSource);
      Reader reader = new InputStreamReader(inputStream);
      XMLConfigBuilder parser = new XMLConfigBuilder(reader);
      Configuration configuration = parser.getConfiguration();
      configuration.setEnvironment(environment);
      configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
      configuration = parser.parse();
      
      return new DefaultSqlSessionFactory(configuration);

    } catch (Exception e) {
      throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }
  

  
  protected SqlSession openSession() {
    SqlSessionFactory sqlMapper = getSessionFactory();
    return sqlMapper.openSession();
  }

  protected ProcessEngineConfiguration getProcessEngineConfiguration() {
    if (DEFAULT_ENGINE.equals(processEngineName)) {
      return ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine()).getProcessEngineConfiguration();
    } else {
      return ((ProcessEngineImpl) ProcessEngines.getProcessEngine(processEngineName)).getProcessEngineConfiguration();
    }
  }
  
  private void performDbSchemaCreation(CycleDbSqlSessionFactory dbSqlSessionFactory, ProcessEngineConfiguration processEngineConfiguration) {
    String dbSchemaStrategy = processEngineConfiguration.getDatabaseSchemaUpdate();
    
    if (ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(dbSchemaStrategy)) {
      try {
        dbSqlSessionFactory.dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(dbSchemaStrategy) || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(dbSchemaStrategy)
            || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(dbSchemaStrategy)) {
      dbSqlSessionFactory.dbSchemaCreate();
      
    } else if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE.equals(dbSchemaStrategy)) {
      dbSqlSessionFactory.dbSchemaCheckVersion();
      
    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE.equals(dbSchemaStrategy)) {
      log.warning("Cycle doesn't support '" + ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
              + "' DB strategy at the moment. Nothing is created!");
      // TODO: the check if necessary doesn't work, since the tables are alway created by the engine already!
//      try {
      // dbSqlSessionFactory.dbSchemaCreate();
//        dbSqlSessionFactory.dbSchemaCheckVersion();
//      } catch (Exception e) {
//        if (e.getMessage().indexOf("no activiti tables in db") != -1) {
//          dbSqlSessionFactory.dbSchemaCreate();
//        }
      // }
    }
    

    //
    // if
    // (ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy))
    // {
    // try {
    // dbSqlSessionFactory.dbSchemaDrop();
    // } catch (RuntimeException e) {
    // // ignore
    // }
    // }
    //
    // if (DbSchemaStrategy.CREATE_DROP.equals(dbSchemaStrategy) ||
    // ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy)
    // ||
    // ProcessEngineConfiguration.DBSCHEMASTRATEGY_CREATE.equals(dbSchemaStrategy))
    // {
    // dbSqlSessionFactory.dbSchemaCreate();
    //
    // } else if (DbSchemaStrategy.CHECK_VERSION.equals(dbSchemaStrategy)) {
    // dbSqlSessionFactory.dbSchemaCheckVersion();
    // }
  }
  
}
