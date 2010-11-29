package org.activiti.cycle.impl.connector.view;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.service.CycleService;

public class TagConnectorConfiguration extends RepositoryConnectorConfiguration {

  public static final String TAG_CONNECTOR_ID = "TAG";
  public static final String TAG_CONNECTOR_NAME = "TAGS";
  
  private CycleService cycleService;

  public TagConnectorConfiguration() {
    super.setId(TAG_CONNECTOR_ID);
    super.setName(TAG_CONNECTOR_NAME);
  }

  /**
   * Easiest way to instantiate is to hand in a {@link ConfigurationContainer}
   * for the current user
   */
  public TagConnectorConfiguration(CycleService cycleService) {
    this.cycleService = cycleService;
    super.setId(TAG_CONNECTOR_ID);
    super.setName(TAG_CONNECTOR_NAME);
  }

  @Override
  public RepositoryConnector createConnector() {
    return new TagConnector(this);
  } 

  public CycleService getCycleService() {
    return cycleService;
  }
}
