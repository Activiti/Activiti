package org.activiti.cycle.impl.db;

import java.util.HashMap;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.CycleConfigEntity;
import org.activiti.cycle.impl.conf.CycleDbSqlSessionFactory;
import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.thoughtworks.xstream.XStream;

/**
 * First implementation of {@link CycleService} using the database in the
 * background to write stuff as XML in the database.
 */
public class CycleServiceDbXStreamImpl extends DummyBaseCycleService implements CycleService {
  
  private XStream xStream = new XStream();
  
  private String processEngineName = null;
  private static String DEFAULT_ENGINE = "DEFAULT_PROCESS_ENGINE";
  
  private static HashMap<String, CycleDbSqlSessionFactory> dbFactories = new HashMap<String, CycleDbSqlSessionFactory>();

  public CycleServiceDbXStreamImpl(String processEngineName) {
    if (processEngineName == null) {
      this.processEngineName = DEFAULT_ENGINE;
    } else {
      this.processEngineName = processEngineName;
    }
  }
  
  public CycleServiceDbXStreamImpl() {
    this(DEFAULT_ENGINE);
  }
    
  private SqlSessionFactory getSessionFactory() {
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
  
  public XStream getXStream() {
    return xStream;
  }
  
  public void saveConfiguration(ConfigurationContainer container) {
    createAndInsert(container, container.getName());
  }

  public ConfigurationContainer getConfiguration(String name) {
    CycleConfigEntity cycleConfig = selectById(name);
    Object configXML = getXStream().fromXML(cycleConfig.getConfigXML());
    return (ConfigurationContainer) configXML;
  }

  //----- start implementation for cycle persistence -----
  
  public ProcessEngineConfiguration getProcessEngineConfiguration() {
    if (DEFAULT_ENGINE.equals(processEngineName)) {
      return ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine()).getProcessEngineConfiguration();
    } else {
      return ((ProcessEngineImpl) ProcessEngines.getProcessEngine(processEngineName)).getProcessEngineConfiguration();
    }
  }
  
  public CycleConfigEntity selectById(String id) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    try {
      return (CycleConfigEntity) session.selectOne(
              "org.activiti.cycle.impl.conf.CycleConfigEntity.selectCycleConfigById", id);

    } finally {
      session.close();
    }
  }
  
  public void createAndInsert(Object o, String id) {
    CycleConfigEntity cycleConfig = new CycleConfigEntity();
    cycleConfig.setId(id);
    String configXML = getXStream().toXML(o);
    cycleConfig.setConfigXML(configXML);
    
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    try {
      session.insert("org.activiti.cycle.impl.conf.CycleConfigEntity.insertCycleConfig", cycleConfig);    
      session.commit();
    } finally {
      session.close();
    }
  }
  
  public void updateById(CycleConfigEntity cycleConfig) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    try {
      session.update(
              "org.activiti.cycle.impl.conf.CycleConfigEntity.updateCycleConfigById", cycleConfig);
      session.commit();
    } finally {
      session.close();
    }
  }
  
  public void deleteById(String id) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    try {
      session.delete(
              "org.activiti.cycle.impl.conf.CycleConfigEntity.deleteCycleConfigById", id);
      session.commit();
    } finally {
      session.close();
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
    
    if ( DbSchemaStrategy.CREATE_DROP.equals(dbSchemaStrategy) 
         || ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy)
         || ProcessEngineConfiguration.DBSCHEMASTRATEGY_CREATE.equals(dbSchemaStrategy)
       ) {
      dbSqlSessionFactory.dbSchemaCreate();
      
    } else if (DbSchemaStrategy.CHECK_VERSION.equals(dbSchemaStrategy)) {
      dbSqlSessionFactory.dbSchemaCheckVersion();
    }    
  }

}
