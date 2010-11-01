package org.activiti.cycle.impl.db.impl;

import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.db.CycleConfigurationService;
import org.activiti.cycle.impl.db.entity.CycleConfigEntity;
import org.apache.ibatis.session.SqlSession;

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
    CycleConfigEntity cycleConfig = selectById(name);
    Object configXML = this.xStream.fromXML(cycleConfig.getConfigXML());
    return (ConfigurationContainer) configXML;
  }

  private CycleConfigEntity selectById(String id) {
    SqlSession session = openSession();
    try {
      return (CycleConfigEntity) session.selectOne("selectCycleConfigById", id);

    } finally {
      session.close();
    }
  }

  private void createAndInsert(Object o, String id) {
    CycleConfigEntity cycleConfig = new CycleConfigEntity();
    cycleConfig.setId(id);
    String configXML = this.xStream.toXML(o);
    cycleConfig.setConfigXML(configXML);

    SqlSession session = openSession();
    try {
      session.insert("insertCycleConfig", cycleConfig);
      session.commit();
    } finally {
      session.close();
    }
  }

  private void updateById(CycleConfigEntity cycleConfig) {
    SqlSession session = openSession();
    try {
      session.update("updateCycleConfigById", cycleConfig);
      session.commit();
    } finally {
      session.close();
    }
  }

  private void deleteById(String id) {
    SqlSession session = openSession();
    try {
      session.delete("deleteCycleConfigById", id);
      session.commit();
    } finally {
      session.close();
    }
  }

  

  

}
