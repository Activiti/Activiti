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

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Activiti JMX service agent
 */

/**
 * @author Saeid Mirzaei
 */
public interface ManagementAgent {

  /**
   * Registers object with management infrastructure with a specific name.
   * Object must be annotated or implement standard MBean interface.
   * 
   * @param obj
   *          the object to register
   * @param name
   *          the name
   * @throws JMException
   *           is thrown if the registration failed
   */
  void register(Object obj, ObjectName name) throws JMException;

  /**
   * Registers object with management infrastructure with a specific name.
   * Object must be annotated or implement standard MBean interface.
   * 
   * @param obj
   *          the object to register
   * @param name
   *          the name
   * @param forceRegistration
   *          if set to <tt>true</tt>, then object will be registered despite
   *          existing object is already registered with the name.
   * @throws JMException
   *           is thrown if the registration failed
   */
  void register(Object obj, ObjectName name, boolean forceRegistration) throws JMException;

  /**
   * Unregisters object based upon registered name
   * 
   * @param name
   *          the name
   * @throws JMException
   *           is thrown if the unregistration failed
   */
  void unregister(ObjectName name) throws JMException;

  /**
   * Is the given object registered
   * 
   * @param name
   *          the name
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
   * @param mbeanServer
   *          the custom mbean server
   */
  void setMBeanServer(MBeanServer mbeanServer);

  public void findAndRegisterMbeans() throws Exception;

  public void doStart();

}
