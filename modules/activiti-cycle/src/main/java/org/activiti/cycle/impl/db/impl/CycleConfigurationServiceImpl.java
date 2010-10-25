package org.activiti.cycle.impl.db.impl;

import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.db.CycleConfigurationService;
import org.activiti.cycle.impl.db.entity.CycleConfig;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.thoughtworks.xstream.XStream;

public class CycleConfigurationServiceImpl extends AbstractCycleDaoMyBatisImpl implements CycleConfigurationService {

  
  private XStream xStream = new XStream();

  public CycleConfigurationServiceImpl(String processEngineName) {
    if(processEngineName != null) {
      this.processEngineName = processEngineName;
    }
  }

  public void saveConfiguration(ConfigurationContainer container) {
    createAndInsert(container, container.getName());
  }

  public ConfigurationContainer getConfiguration(String name) {
    CycleConfig cycleConfig = selectById(name);
    Object configXML = this.xStream.fromXML(cycleConfig.getConfigXML());
    return (ConfigurationContainer) configXML;
  }

  private CycleConfig selectById(String id) {
    SqlSessionFactory sqlMapper = getSessionFactory();

    SqlSession session = sqlMapper.openSession();
    try {
      return (CycleConfig) session.selectOne("org.activiti.cycle.impl.db.entity.CycleConfig.selectCycleConfigById", id);

    } finally {
      session.close();
    }
  }

  private void createAndInsert(Object o, String id) {
    CycleConfig cycleConfig = new CycleConfig();
    cycleConfig.setId(id);
    String configXML = this.xStream.toXML(o);
    cycleConfig.setConfigXML(configXML);

    SqlSessionFactory sqlMapper = getSessionFactory();

    SqlSession session = sqlMapper.openSession();
    try {
      session.insert("org.activiti.cycle.impl.db.entity.CycleConfig.insertCycleConfig", cycleConfig);
      session.commit();
    } finally {
      session.close();
    }
  }

  private void updateById(CycleConfig cycleConfig) {
    SqlSessionFactory sqlMapper = getSessionFactory();

    SqlSession session = sqlMapper.openSession();
    try {
      session.update("org.activiti.cycle.impl.db.entity.CycleConfig.updateCycleConfigById", cycleConfig);
      session.commit();
    } finally {
      session.close();
    }
  }

  private void deleteById(String id) {
    SqlSessionFactory sqlMapper = getSessionFactory();

    SqlSession session = sqlMapper.openSession();
    try {
      session.delete("org.activiti.cycle.impl.db.entity.CycleConfig.deleteCycleConfigById", id);
      session.commit();
    } finally {
      session.close();
    }
  }

  

  

}
