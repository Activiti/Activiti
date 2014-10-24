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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Saeid Mirzaei
 */

public class DefaultManagementAgent implements ManagementAgent {

  private final String DEFAULT_HOST = "localhost";

  private static final Logger LOG = LoggerFactory.getLogger(DefaultManagementAgent.class);
  private MBeanServer server;
  private final ConcurrentMap<ObjectName, ObjectName> mbeansRegistered = new ConcurrentHashMap<ObjectName, ObjectName>();
  JMXConfigurator jmxConfigurator;

  public DefaultManagementAgent(JMXConfigurator jmxConfigurator) {
    this.jmxConfigurator = jmxConfigurator;

  }

  public void register(Object obj, ObjectName name) throws JMException {
    register(obj, name, false);
  }

  public void register(Object obj, ObjectName name, boolean forceRegistration) throws JMException {
    try {
      registerMBeanWithServer(obj, name, forceRegistration);
    } catch (NotCompliantMBeanException e) {
      LOG.error("Mbean " + name + " is not compliant MBean.");

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

  protected void createMBeanServer() {
    String hostName;
    boolean canAccessSystemProps = true;
    try {
      // we'll do it this way mostly to determine if we should lookup the
      // hostName
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPropertiesAccess();
      }
    } catch (SecurityException se) {
      canAccessSystemProps = false;
    }

    if (canAccessSystemProps) {
      try {
        hostName = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException uhe) {
        LOG.info("Cannot determine localhost name. Fallback to: " + DEFAULT_HOST, uhe);
        hostName = DEFAULT_HOST;
      }
    } else {
      hostName = DEFAULT_HOST;
    }

    server = findOrCreateMBeanServer();

    try {
      // Create the connector if we need
      if (createConnector) {
        createJmxConnector(hostName);
      }
    } catch (IOException ioe) {
      LOG.warn("Could not create and start JMX connector.", ioe);
    }
  }

  protected MBeanServer findOrCreateMBeanServer() {


    // look for the first mbean server that has match default domain name
    List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

    for (MBeanServer server : servers) {
      LOG.debug("Found MBeanServer with default domain {}", server.getDefaultDomain());

      if (mBeanServerDefaultDomain.equals(server.getDefaultDomain())) {
        return server;
      }
    }

    // create a mbean server with the given default domain name
    return MBeanServerFactory.createMBeanServer(mBeanServerDefaultDomain);
  }

}
