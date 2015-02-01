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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.modelmbean.RequiredModelMBean;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Saeid Mirzaei
 */

public class DefaultManagementAgentTest {

  @Mock
  MBeanServer mbeanServer;

  @Mock
  ObjectInstance instance;

  Object object = "object";
  ObjectName sourceObjectName;
  ObjectName registeredObjectName;
  ManagementAgent agent;

  @Before
  public void initMocks() throws MalformedObjectNameException {
    MockitoAnnotations.initMocks(this);
    sourceObjectName = new ObjectName("domain", "key", "value");
    registeredObjectName = new ObjectName("domain", "key", "otherValue");
    JMXConfigurator jmxConfigurator = new JMXConfigurator();
    agent = new DefaultManagementAgent(jmxConfigurator);
    agent.setMBeanServer(mbeanServer);

  }

  @Test
  public void testRegisterandUnregister() throws JMException {
    reset();

    // Register MBean and return different ObjectName
    when(mbeanServer.isRegistered(sourceObjectName)).thenReturn(false);
    when(mbeanServer.registerMBean(any(RequiredModelMBean.class), any(ObjectName.class))).thenReturn(instance);

    when(instance.getObjectName()).thenReturn(registeredObjectName);
    when(mbeanServer.isRegistered(registeredObjectName)).thenReturn(true);

    agent.register(object, sourceObjectName);

    verify(mbeanServer).isRegistered(sourceObjectName);
    verify(mbeanServer).registerMBean(any(RequiredModelMBean.class), any(ObjectName.class));

    assertTrue(agent.isRegistered(sourceObjectName));

    agent.unregister(sourceObjectName);
    verify(mbeanServer).unregisterMBean(registeredObjectName);
    assertFalse(agent.isRegistered(sourceObjectName));

  }

  @Test
  public void testRegisterExisting() throws JMException {
    reset(mbeanServer, instance);

    // do not try to reregister it, if it already exists
    when(mbeanServer.isRegistered(sourceObjectName)).thenReturn(true);
    agent.register(object, sourceObjectName);
    verify(mbeanServer, never()).registerMBean(object, sourceObjectName);
  }

  @Test
  public void testUnRegisterNotExisting() throws JMException {
    reset(mbeanServer, instance);

    // ... do not unregister if it does not exist
    when(mbeanServer.isRegistered(sourceObjectName)).thenReturn(false);
    when(instance.getObjectName()).thenReturn(registeredObjectName);

    agent.unregister(sourceObjectName);
    verify(mbeanServer).isRegistered(sourceObjectName);
    verify(mbeanServer, never()).unregisterMBean(registeredObjectName);

    assertFalse(agent.isRegistered(sourceObjectName));

  }

}
