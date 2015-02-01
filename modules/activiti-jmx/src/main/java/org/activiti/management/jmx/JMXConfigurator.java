/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.management.jmx;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Saeid Mirzaei
 */

public class JMXConfigurator extends AbstractProcessEngineConfigurator {
  
  public static final String DEFAUL_JMX_DOMAIN = "DefaultDomain";
  
  //jmx (rmi server connection) port
  protected Integer connectorPort = -1;

  // jmx domain name
  protected String domain = "org.activiti.jmx.Mbeans";
  
  // the domain name for the activiti mbeans
  protected String mbeanDomain = DEFAUL_JMX_DOMAIN;

  // JMX service URL path
  protected String serviceUrlPath = "/jmxrmi/activiti";

  protected Boolean createConnector = true;

  protected ProcessEngineConfiguration processEngineConfig;

  protected ManagementAgent managementAgent;

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
  private Integer registryPort = 1099;

  public Integer getRegistryPort() {
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
