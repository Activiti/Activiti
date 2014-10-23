package org.activiti.management;

import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;

import org.omg.CORBA.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultManagementAgent {

  public static final String DEFAULT_DOMAIN = "org.activiti";
  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_REGISTRY_PORT = 1099;
  public static final int DEFAULT_CONNECTION_PORT = -1;
  public static final String DEFAULT_SERVICE_URL_PATH = "/jmxrmi/activiti";
  private static final Logger LOG = LoggerFactory.getLogger(DefaultManagementAgent.class);

  private MBeanServer server;

  private final ConcurrentMap<ObjectName, ObjectName> mbeansRegistered = new ConcurrentHashMap<ObjectName, ObjectName>();
  private JMXConnectorServer cs;
  private Registry registry;

  private Integer registryPort;
  private Integer connectorPort;
  private String mBeanServerDefaultDomain;
  private String mBeanObjectDomainName;
  private String serviceUrlPath;
  private Boolean usePlatformMBeanServer = true;
  private Boolean createConnector;
  private Boolean onlyRegisterProcessorWithCustomId;
  private Boolean registerAlways;
  private Boolean registerNewRoutes = true;
  private Boolean mask;
  private Boolean includeHostName;

  public DefaultManagementAgent() {
  }

  protected void finalizeSettings() {
    if (registryPort == null) {
      registryPort = Integer.getInteger(JmxSystemPropertyKeys.REGISTRY_PORT, DEFAULT_REGISTRY_PORT);
    }
    if (connectorPort == null) {
      connectorPort = Integer.getInteger(JmxSystemPropertyKeys.CONNECTOR_PORT, DEFAULT_CONNECTION_PORT);
    }
    if (mBeanServerDefaultDomain == null) {
      mBeanServerDefaultDomain = System.getProperty(JmxSystemPropertyKeys.DOMAIN, DEFAULT_DOMAIN);
    }
    if (mBeanObjectDomainName == null) {
      mBeanObjectDomainName = System.getProperty(JmxSystemPropertyKeys.MBEAN_DOMAIN, DEFAULT_DOMAIN);
    }
    if (serviceUrlPath == null) {
      serviceUrlPath = System.getProperty(JmxSystemPropertyKeys.SERVICE_URL_PATH, DEFAULT_SERVICE_URL_PATH);
    }
    if (createConnector == null) {
      createConnector = Boolean.getBoolean(JmxSystemPropertyKeys.CREATE_CONNECTOR);
    }
    if (onlyRegisterProcessorWithCustomId == null) {
      onlyRegisterProcessorWithCustomId = Boolean.getBoolean(JmxSystemPropertyKeys.ONLY_REGISTER_PROCESSOR_WITH_CUSTOM_ID);
    }
    // "Use platform mbean server" is true by default
    if (System.getProperty(JmxSystemPropertyKeys.USE_PLATFORM_MBS) != null) {
      usePlatformMBeanServer = Boolean.getBoolean(JmxSystemPropertyKeys.USE_PLATFORM_MBS);
    }
    if (System.getProperty(JmxSystemPropertyKeys.REGISTER_ALWAYS) != null) {
      registerAlways = Boolean.getBoolean(JmxSystemPropertyKeys.REGISTER_ALWAYS);
    }
    if (System.getProperty(JmxSystemPropertyKeys.REGISTER_NEW_ROUTES) != null) {
      registerNewRoutes = Boolean.getBoolean(JmxSystemPropertyKeys.REGISTER_NEW_ROUTES);
    }
    if (System.getProperty(JmxSystemPropertyKeys.MASK) != null) {
      mask = Boolean.getBoolean(JmxSystemPropertyKeys.MASK);
    }
    if (System.getProperty(JmxSystemPropertyKeys.INCLUDE_HOST_NAME) != null) {
      includeHostName = Boolean.getBoolean(JmxSystemPropertyKeys.INCLUDE_HOST_NAME);
    }
    if (System.getProperty(JmxSystemPropertyKeys.CREATE_CONNECTOR) != null) {
      createConnector = Boolean.getBoolean(JmxSystemPropertyKeys.CREATE_CONNECTOR);
    }
  }

  public void setRegistryPort(Integer port) {
    registryPort = port;
  }

  public Integer getRegistryPort() {
    return registryPort;
  }

  public void setConnectorPort(Integer port) {
    connectorPort = port;
  }

  public Integer getConnectorPort() {
    return connectorPort;
  }

  public void setMBeanServerDefaultDomain(String domain) {
    mBeanServerDefaultDomain = domain;
  }

  public String getMBeanServerDefaultDomain() {
    return mBeanServerDefaultDomain;
  }

  public void setMBeanObjectDomainName(String domainName) {
    mBeanObjectDomainName = domainName;
  }

  public String getMBeanObjectDomainName() {
    return mBeanObjectDomainName;
  }

  public void setServiceUrlPath(String url) {
    serviceUrlPath = url;
  }

  public String getServiceUrlPath() {
    return serviceUrlPath;
  }

  public void setCreateConnector(Boolean flag) {
    createConnector = flag;
  }

  public Boolean getCreateConnector() {
    return createConnector;
  }

  public void setUsePlatformMBeanServer(Boolean flag) {
    usePlatformMBeanServer = flag;
  }

  public Boolean getUsePlatformMBeanServer() {
    return usePlatformMBeanServer;
  }

  public Boolean getOnlyRegisterProcessorWithCustomId() {
    return onlyRegisterProcessorWithCustomId;
  }

  public void setOnlyRegisterProcessorWithCustomId(Boolean onlyRegisterProcessorWithCustomId) {
    this.onlyRegisterProcessorWithCustomId = onlyRegisterProcessorWithCustomId;
  }

  public void setMBeanServer(MBeanServer mbeanServer) {
    server = mbeanServer;
  }

  public MBeanServer getMBeanServer() {
    return server;
  }

  public Boolean getRegisterAlways() {
    return registerAlways != null && registerAlways;
  }

  public void setRegisterAlways(Boolean registerAlways) {
    this.registerAlways = registerAlways;
  }

  public Boolean getRegisterNewRoutes() {
    return registerNewRoutes != null && registerNewRoutes;
  }

  public void setRegisterNewRoutes(Boolean registerNewRoutes) {
    this.registerNewRoutes = registerNewRoutes;
  }

  public Boolean getMask() {
    return mask != null && mask;
  }

  public void setMask(Boolean mask) {
    this.mask = mask;
  }

  public Boolean getIncludeHostName() {
    return includeHostName != null && includeHostName;
  }

  public void setIncludeHostName(Boolean includeHostName) {
    this.includeHostName = includeHostName;
  }

  public void register(Object obj, ObjectName name) throws JMException {
    register(obj, name, false);
  }

  public void register(Object obj, ObjectName name, boolean forceRegistration) throws JMException {
    try {
      registerMBeanWithServer(obj, name, forceRegistration);
    } catch (NotCompliantMBeanException e) {
      // If this is not a "normal" MBean, then try to deploy it using JMX
      // annotations
      ManagementMBeanAssembler assembler = camelContext.getManagementMBeanAssembler();
      ObjectHelper.notNull(assembler, "ManagementMBeanAssembler", camelContext);
      Object mbean = assembler.assemble(server, obj, name);
      if (mbean != null) {
        // and register the mbean
        registerMBeanWithServer(mbean, name, forceRegistration);
      }
    }
  }

  private void registerMBeanWithServer(Object obj, ObjectName name, boolean forceRegistration) throws JMException {

    // have we already registered the bean, there can be shared instances in the
    // camel routes
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

}
