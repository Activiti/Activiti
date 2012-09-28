package org.activiti.engine.test.db;

import org.apache.ibatis.datasource.pooled.PooledDataSource;


public class IdGeneratorDataSource extends PooledDataSource {

  public IdGeneratorDataSource() {
    setDriver("org.h2.Driver");
    setUrl("jdbc:h2:mem:activiti");
    setUsername("sa");
    setPassword("");
    setPoolMaximumActiveConnections(2);
  }
}
