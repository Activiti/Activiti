package org.activiti.cycle.impl.db.impl;

import java.util.HashMap;

import org.activiti.cycle.impl.conf.CycleDbSqlSessionFactory;
import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;


public abstract class AbstractCycleDaoMyBatisImpl {
  
  private static HashMap<String, CycleDbSqlSessionFactory> dbFactories = new HashMap<String, CycleDbSqlSessionFactory>();
  protected String processEngineName = DEFAULT_ENGINE;
  protected static String DEFAULT_ENGINE = "DEFAULT_PROCESS_ENGINE";
  
  protected SqlSessionFactory getSessionFactory() {
    if (dbFactories.get(processEngineName) == null) {
      synchronized (dbFactories) {
        // lazy initialization, only done once per proces engine!
        if (dbFactories.get(processEngineName) == null) {
          CycleDbSqlSessionFactory factory = new CycleDbSqlSessionFactory();
          factory.configurationCompleted(getProcessEngineConfiguration());
          performDbSchemaCreation(factory, getProcessEngineConfiguration());
          dbFactories.put(processEngineName, factory);
        }
      }
    }
    return dbFactories.get(processEngineName).getSqlSessionFactory();
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
    String dbSchemaStrategy = processEngineConfiguration.getDbSchemaStrategy();

    if (ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy)) {
      try {
        dbSqlSessionFactory.dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }

    if (DbSchemaStrategy.CREATE_DROP.equals(dbSchemaStrategy) || ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy)
            || ProcessEngineConfiguration.DBSCHEMASTRATEGY_CREATE.equals(dbSchemaStrategy)) {
      dbSqlSessionFactory.dbSchemaCreate();

    } else if (DbSchemaStrategy.CHECK_VERSION.equals(dbSchemaStrategy)) {
      dbSqlSessionFactory.dbSchemaCheckVersion();
    }
  }
  
}
