package org.activiti.management;


public final class JmxSystemPropertyKeys {

    // disable jmx
    public static final String DISABLED = "org.activiti.jmx.disabled";

    // jmx (rmi registry) port
    public static final String REGISTRY_PORT = "org.activiti.jmx.rmiConnector.registryPort";
    
    // jmx (rmi server connection) port
    public static final String CONNECTOR_PORT = "org.activiti.jmx.rmiConnector.connectorPort";

    // jmx domain name
    public static final String DOMAIN = "org.activiti.jmx.mbeanServerDefaultDomain";
    
    // the domain name for the camel mbeans
    public static final String MBEAN_DOMAIN = "org.activiti.jmx.mbeanObjectDomainName";

    // JMX service URL path 
    public static final String SERVICE_URL_PATH = "org.activiti.jmx.serviceUrlPath";
    
    // A flag that indicates whether the agent should be created
    public static final String CREATE_CONNECTOR = "org.activiti.jmx.createRmiConnector";
    
    // use jvm platform mbean server flag
    public static final String USE_PLATFORM_MBS = "org.activiti.jmx.usePlatformMBeanServer";

    // whether all processors or only processors with a custom id given should be registered
    public static final String ONLY_REGISTER_PROCESSOR_WITH_CUSTOM_ID = "org.activiti.jmx.onlyRegisterProcessorWithCustomId";

    // whether to register always
    public static final String REGISTER_ALWAYS = "org.activiti.jmx.registerAlways";

    // whether to register when starting new routes
    public static final String REGISTER_NEW_ROUTES = "org.activiti.jmx.registerNewRoutes";

    // Whether to remove detected sensitive information (such as passwords) from MBean names and attributes.
    public static final String MASK = "org.activiti.jmx.mask";

    // Whether to include host name in MBean naes
    public static final String INCLUDE_HOST_NAME = "org.activiti.jmx.includeHostName";

    private JmxSystemPropertyKeys() {
        // not instantiated
    }

}
