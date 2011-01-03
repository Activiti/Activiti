package org.activiti.cycle.impl.connector.view;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

public class TagConnectorConfiguration extends RepositoryConnectorConfiguration {

  public static final String TAG_CONNECTOR_ID = "TAG";
  public static final String TAG_CONNECTOR_NAME = "TAGS";

  /**
   * Easiest way to instantiate is to hand in a {@link ConfigurationContainer}
   * for the current user
   */
  public TagConnectorConfiguration() {
    super.setId(TAG_CONNECTOR_ID);
    super.setName(TAG_CONNECTOR_NAME);
  }

  @Override
  public RepositoryConnector createConnector() {
    return new TagConnector(this);
  }

}
