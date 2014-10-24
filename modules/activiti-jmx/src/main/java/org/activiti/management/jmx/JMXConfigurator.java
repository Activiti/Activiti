package org.activiti.management.jmx;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXConfigurator extends AbstractProcessEngineConfigurator {

  private static final Logger LOG = LoggerFactory.getLogger(JMXConfigurator.class);

  // disable jmx
  private boolean disabled = false;

  // jmx (rmi registry) port
  private int registryPort = 1099;

  // jmx (rmi server connection) port
  private int connectorPort = -1;

  // jmx domain name
  private String domain = "org.activiti";

  // the domain name for the activiti mbeans
  private String mbeanDomain;

  // JMX service URL path
  private String serviceUrlPath = "/jmxrmi/activiti";

  ProcessEngineConfiguration processEngineConfig;

  ManagementAgent managementAgent;

  @Override
  public void beforeInit(ProcessEngineConfigurationImpl arg0) {
    // nothing to do
  }


  }
  @Override
  public void configure(ProcessEngineConfigurationImpl arg0) {
    processEngineConfig = arg0;
    if (!disabled) {
      managementAgent = new DefaultManagementAgent(this);
    }

  }

}
