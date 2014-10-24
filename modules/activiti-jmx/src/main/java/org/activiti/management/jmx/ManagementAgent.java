package org.activiti.management.jmx;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Camel JMX service agent
 */
public interface ManagementAgent {

    /**
     * Registers object with management infrastructure with a specific name. Object must be annotated or 
     * implement standard MBean interface.
     *
     * @param obj  the object to register
     * @param name the name
     * @throws JMException is thrown if the registration failed
     */
    void register(Object obj, ObjectName name) throws JMException;
    
    /**
     * Registers object with management infrastructure with a specific name. Object must be annotated or 
     * implement standard MBean interface.
     *
     * @param obj  the object to register
     * @param name the name
     * @param forceRegistration if set to <tt>true</tt>, then object will be registered despite
     * existing object is already registered with the name.
     * @throws JMException is thrown if the registration failed
     */
    void register(Object obj, ObjectName name, boolean forceRegistration) throws JMException;
    
    /**
     * Unregisters object based upon registered name
     *
     * @param name the name
     * @throws JMException is thrown if the unregistration failed
     */
    void unregister(ObjectName name) throws JMException;

    /**
     * Is the given object registered
     *
     * @param name the name
     * @return <tt>true</tt> if registered
     */
    boolean isRegistered(ObjectName name);

    /**
     * Get the MBeanServer which hosts managed objects.
     * <p/>
     * <b>Notice:</b> If the JMXEnabled configuration is not set to <tt>true</tt>,
     * this method will return <tt>null</tt>.
     * 
     * @return the MBeanServer
     */
    MBeanServer getMBeanServer();

    /**
     * Sets a custom mbean server to use
     *
     * @param mbeanServer the custom mbean server
     */
    void setMBeanServer(MBeanServer mbeanServer);

   }
