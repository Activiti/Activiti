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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.activiti.management.jmx.mbeans.JobExecutorMBean;
import org.activiti.management.jmx.mbeans.ProcessDefinitionsMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Saeid Mirzaei
 */

public class DefaultManagementAgent implements ManagementAgent {
  
  private static final Logger LOG = LoggerFactory.getLogger(DefaultManagementAgent.class);
  
  protected MBeanServer server;
  protected final ConcurrentMap<ObjectName, ObjectName> mbeansRegistered = new ConcurrentHashMap<ObjectName, ObjectName>();
  protected JMXConfigurator jmxConfigurator;
  protected Registry registry;
  protected JMXConnectorServer cs;
  protected ManagementMBeanAssembler assembler;

  public DefaultManagementAgent(JMXConfigurator jmxConfigurator) {
    this.jmxConfigurator = jmxConfigurator;
    this.assembler = new DefaultManagementMBeanAssembler();

  }

  public void register(Object obj, ObjectName name) throws JMException {
    register(obj, name, false);
  }

  public void register(Object obj, ObjectName name, boolean forceRegistration) throws JMException {
    try {
      Object mbean = assembler.assemble(obj, name);
      if (mbean != null)
        // and register the mbean
        registerMBeanWithServer(mbean, name, forceRegistration);
      else
        registerMBeanWithServer(obj, name, forceRegistration);

    } catch (NotCompliantMBeanException e) {
      LOG.error("Mbean " + name + " is not compliant MBean.", e);
      registerMBeanWithServer(obj, name, forceRegistration);

    }

  }

  private void registerMBeanWithServer(Object obj, ObjectName name, boolean forceRegistration) throws JMException {

    boolean exists = isRegistered(name);
    if (exists) {
      if (forceRegistration) {
        LOG.info("ForceRegistration enabled, unregistering existing MBean with ObjectName: {}", name);
        server.unregisterMBean(name);
      } else {
        // okay ignore we do not want to force it and it could be a shared
        // instance
        LOG.debug("MBean already registered with ObjectName: {}", name);
      }
    }

    // register bean if by force or not exists
    ObjectInstance instance = null;
    if (forceRegistration || !exists) {
      LOG.trace("Registering MBean with ObjectName: {}", name);
      instance = server.registerMBean(obj, name);
    }

    // need to use the name returned from the server as some JEE servers may
    // modify the name
    if (instance != null) {
      ObjectName registeredName = instance.getObjectName();
      LOG.debug("Registered MBean with ObjectName: {}", registeredName);
      mbeansRegistered.put(name, registeredName);
    }
  }

  public boolean isRegistered(ObjectName name) {
    ObjectName on = mbeansRegistered.get(name);
    return (on != null && server.isRegistered(on)) || server.isRegistered(name);
  }

  public void unregister(ObjectName name) throws JMException {
    if (isRegistered(name)) {
      ObjectName on = mbeansRegistered.remove(name);
      server.unregisterMBean(on);
      LOG.debug("Unregistered MBean with ObjectName: {}", name);
    } else {
      mbeansRegistered.remove(name);
    }
  }

  @Override
  public MBeanServer getMBeanServer() {
    return server;
  }

  @Override
  public void setMBeanServer(MBeanServer mbeanServer) {
    this.server = mbeanServer;
  }

  public void doStart() {
    createMBeanServer();
  }
  

  protected void createMBeanServer() {
    
    server = findOrCreateMBeanServer();
    try {
      // Create the connector if we need
      if (jmxConfigurator.getCreateConnector()) {
        createJmxConnector(Utils.getHostName());
      }
    } catch (IOException ioe) {
      LOG.warn("Could not create and start JMX connector.", ioe);
    }

  }

  protected MBeanServer findOrCreateMBeanServer() {

    // look for the first mbean server that has match default domain name
    if (jmxConfigurator.getMbeanDomain().equals(JMXConfigurator.DEFAUL_JMX_DOMAIN))
      return ManagementFactory.getPlatformMBeanServer();

    List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

    for (MBeanServer server : servers) {
      LOG.debug("Found MBeanServer with default domain {}", server.getDefaultDomain());
      System.out.println(server.getDefaultDomain());

      if (jmxConfigurator.getMbeanDomain().equals(server.getDefaultDomain())) {
        return server;
      }
    }

    // create a mbean server with the given default domain name
    return MBeanServerFactory.createMBeanServer(jmxConfigurator.getMbeanDomain());
  }

  @Override
  public void findAndRegisterMbeans() throws Exception {
	  register(new ProcessDefinitionsMBean(jmxConfigurator.getProcessEngineConfig()), new ObjectName(jmxConfigurator.getDomain(), "type", "Deployments"));
	  register(new JobExecutorMBean(jmxConfigurator.getProcessEngineConfig()), new ObjectName(jmxConfigurator.getDomain(), "type", "JobExecutor"));

  }
  public void createJmxConnector(String host) throws IOException {

    String serviceUrlPath = jmxConfigurator.getServiceUrlPath();
    Integer registryPort = jmxConfigurator.getRegistryPort();
    Integer connectorPort = jmxConfigurator.getConnectorPort();
    if (serviceUrlPath == null) {
      LOG.warn("Service url path is null. JMX connector creation skipped");
      return;
    }
    if (registryPort == null) {
      LOG.warn("Registery port is null. JMX connector creation skipped.");
      return;
    }

    try {
      registry = LocateRegistry.createRegistry(registryPort);
      LOG.debug("Created JMXConnector RMI registry on port {}", registryPort);
    } catch (RemoteException ex) {
      // The registry may had been created, we could get the registry instead
    }

    // must start with leading slash
    String path = serviceUrlPath.startsWith("/") ? serviceUrlPath : "/" + serviceUrlPath;
    // Create an RMI connector and start it
    final JMXServiceURL url;
    if (connectorPort > 0) {
      url = new JMXServiceURL("service:jmx:rmi://" + host + ":" + connectorPort + "/jndi/rmi://" + host + ":" + registryPort + path);
    } else {
      url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + registryPort + path);
    }

    cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);

    // use async thread for starting the JMX Connector
    // (no need to use a thread pool or enlist in JMX as this thread is
    // terminated when the JMX connector has been started)
    Thread thread = new Thread(new Runnable() {

      public void run() {
        try {
          LOG.debug("Staring JMX Connector thread to listen at: {}", url);
          cs.start();
          LOG.info("JMX Connector thread started and listening at: {}", url);
        } catch (IOException ioe) {
          if (ioe.getCause() instanceof javax.naming.NameAlreadyBoundException) {
              LOG.warn("JMX connection:" + url + " already exists.");
          } else {
            LOG.warn("Could not start JMXConnector thread at: " + url + ". JMX Connector not in use.", ioe);
          }
        }
      }
    }, "jmxConnectorStarterThread");
    thread.start();
  }

}
