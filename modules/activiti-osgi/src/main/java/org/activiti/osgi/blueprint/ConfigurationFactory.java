package org.activiti.osgi.blueprint;

import javax.sql.DataSource;

import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;

public class ConfigurationFactory {

    DataSource dataSource;
    String databaseSchemaUpdate;
    boolean jobExecutorActivate = true;

    public StandaloneProcessEngineConfiguration getConfiguration() {
  		StandaloneProcessEngineConfiguration conf =
              new StandaloneProcessEngineConfiguration();
      conf.setDataSource(dataSource);
      conf.setDatabaseSchemaUpdate(databaseSchemaUpdate);
      conf.setJobExecutorActivate(jobExecutorActivate);
      return conf;
    }

    public void setDataSource(DataSource dataSource) {
       this.dataSource = dataSource;
    }

    public void setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
      this.databaseSchemaUpdate = databaseSchemaUpdate;
    }

    public void setJobExecutorActivate(boolean jobExecutorActivate) {
      this.jobExecutorActivate = jobExecutorActivate;
    }
}
