package org.activiti.management.jmx;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXConfigurator extends AbstractProcessEngineConfigurator {

  
  public ProcessEngineConfiguration getProcessEngineConfig() {
    return processEngineConfig;
  }



  
  public void setProcessEngineConfig(ProcessEngineConfiguration processEngineConfig) {
    this.processEngineConfig = processEngineConfig;
  }


  private static final Logger LOG = LoggerFactory.getLogger(JMXConfigurator.class);

  // disable jmx
  private boolean disabled = false;

  
  public String getDomain() {
    return domain;
  }


  
  public void setDomain(String domain) {
    this.domain = domain;
  }


  
  public String getMbeanDomain() {
    return mbeanDomain;
  }


  
  
  public Boolean getCreateConnector() {
    return createConnector;
  }



  
  public void setCreateConnector(Boolean createConnector) {
    this.createConnector = createConnector;
  }



  public void setMbeanDomain(String mbeanDomain) {
    this.mbeanDomain = mbeanDomain;
  }


  // jmx (rmi registry) port
  private Integer  registryPort = 1099;

  
  public Integer  getRegistryPort() {
    return registryPort;
  }



  
  public void setRegistryPort(Integer registryPort) {
    this.registryPort = registryPort;
  }



  
  public String getServiceUrlPath() {
    return serviceUrlPath;
  }



  
  public void setServiceUrlPath(String serviceUrlPath) {
    this.serviceUrlPath = serviceUrlPath;
  }


  
  public Integer getConnectorPort() {
    return connectorPort;
  }



  
  public void setConnectorPort(Integer connectorPort) {
    this.connectorPort = connectorPort;
  }


  // jmx (rmi server connection) port
  private Integer connectorPort = -1;

  // jmx domain name
  private String domain = "org.activiti.jmx.mbeanObjectDomainName";

  public static final String DEFAUL_JMX_DOMAIN = "DefaultDomain"; 
  // the domain name for the activiti mbeans
  private String mbeanDomain = DEFAUL_JMX_DOMAIN;

  // JMX service URL path
  private String serviceUrlPath = "/jmxrmi/activiti";
  
  private Boolean createConnector = true;




  ProcessEngineConfiguration processEngineConfig;

  ManagementAgent managementAgent;

  @Override
  public void beforeInit(ProcessEngineConfigurationImpl arg0) {
    // nothing to do
  }


  @Override
  public void configure(ProcessEngineConfigurationImpl processEngineConfig) {
    try {
      this.processEngineConfig = processEngineConfig;
      if (!disabled) {
        managementAgent = new DefaultManagementAgent(this);
        managementAgent.doStart();
        
        managementAgent.findAndRegisterMbeans();
      }
    } catch (Exception e) {
        LOG.warn("error in initializing jmx. Continue with partial or no JMX configuration", e);
    }

  }

}
