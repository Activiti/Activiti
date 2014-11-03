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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

import org.activiti.management.jmx.annotations.NotificationSenderAware;
import org.activiti.management.jmx.testMbeans.TestMbean;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author Saeid Mirzaei
 */

public class DefaultManagementMBeanAssemblerTest {

  DefaultManagementMBeanAssembler defaultManagementMBeanAssembler = new DefaultManagementMBeanAssembler();

  @Test
  public void testHappyPath() throws MalformedObjectNameException, JMException {
    TestMbean testMbean = new TestMbean();
    ModelMBean mbean = defaultManagementMBeanAssembler.assemble(testMbean, new ObjectName("org.activiti.jmx.Mbeans:type=something"));
    assertNotNull(mbean);
    assertNotNull(mbean.getMBeanInfo());
    assertNotNull(mbean.getMBeanInfo().getAttributes());
    MBeanAttributeInfo[] attributes = mbean.getMBeanInfo().getAttributes();
    assertEquals(2, attributes.length);
    assertTrue((attributes[0].getName().equals("TestAttributeString") && attributes[1].getName().equals("TestAttributeBoolean") || (attributes[1].getName()
            .equals("TestAttributeString") && attributes[0].getName().equals("TestAttributeBoolean"))));
    assertNotNull(mbean.getMBeanInfo().getOperations());
    MBeanOperationInfo[] operations = mbean.getMBeanInfo().getOperations();
    assertNotNull(operations);
    assertEquals(3, operations.length);

  }

  @Test
  public void testNotificationAware() throws MalformedObjectNameException, JMException {
    NotificationSenderAware mockedNotificationAwareMbean = mock(NotificationSenderAware.class);
    ModelMBean modelBean = defaultManagementMBeanAssembler.assemble(mockedNotificationAwareMbean, new ObjectName("org.activiti.jmx.Mbeans:type=something"));
    assertNotNull(modelBean);
    ArgumentCaptor<NotificationSender> argument = ArgumentCaptor.forClass(NotificationSender.class);
    verify(mockedNotificationAwareMbean).setNotificationSender(argument.capture());
    assertNotNull(argument);
    assertNotNull(argument.getValue());

  }

}
