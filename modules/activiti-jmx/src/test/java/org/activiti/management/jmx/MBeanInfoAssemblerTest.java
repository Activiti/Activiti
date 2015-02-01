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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.activiti.management.jmx.testMbeans.BadAttributeGetterHavinParameter;
import org.activiti.management.jmx.testMbeans.BadAttributeGetterNameNotCapital;
import org.activiti.management.jmx.testMbeans.BadAttributeNameNoGetterSetter;
import org.activiti.management.jmx.testMbeans.BadAttributeSetterHavinReturn;
import org.activiti.management.jmx.testMbeans.BadAttributeSetterNameNotCapital;
import org.activiti.management.jmx.testMbeans.BadAttributeVoid;
import org.activiti.management.jmx.testMbeans.BadInherited;
import org.activiti.management.jmx.testMbeans.NotManagedMBean;
import org.activiti.management.jmx.testMbeans.TestMbean;
import org.junit.Test;

/**
 * @author Saeid Mirzaei
 */

public class MBeanInfoAssemblerTest {

  protected TestMbean testMbean = new TestMbean();
  protected MBeanInfoAssembler mbeanInfoAssembler = new MBeanInfoAssembler();

  @Test
  public void testNullInputs() throws JMException {

    // at least one of the first parameters should be not null
    assertNull(mbeanInfoAssembler.getMBeanInfo(null, null, ""));

    // mbean should be not null
    assertNull(mbeanInfoAssembler.getMBeanInfo(testMbean, testMbean, null));

    // it should return something if at least one of the first paramaters are
    // not null
    NotManagedMBean notManagedMbean = new NotManagedMBean();
    assertNotNull(mbeanInfoAssembler.getMBeanInfo(null, notManagedMbean, "someName"));
    assertNotNull(mbeanInfoAssembler.getMBeanInfo(notManagedMbean, null, "someName"));

  }
  @Test
  public void testReadAtributeInfoHappyPath() throws JMException {
    ModelMBeanInfo beanInfo = mbeanInfoAssembler.getMBeanInfo(testMbean, null, "someName");
    assertNotNull(beanInfo);

    assertEquals("test description", beanInfo.getDescription());
    MBeanAttributeInfo[] testAttributes = beanInfo.getAttributes();
    assertNotNull(testAttributes);
    assertEquals(2, testAttributes.length);

    int counter = 0;
    for (MBeanAttributeInfo info : testAttributes) {
      if (info.getName().equals("TestAttributeBoolean")) {
        counter++;
        assertEquals("test attribute Boolean description", info.getDescription());
        assertEquals("java.lang.Boolean", info.getType());
        assertTrue(info.isReadable());
        assertFalse(info.isWritable());
      } else if (info.getName().equals("TestAttributeString")) {
        counter++;
        assertEquals("test attribute String description", info.getDescription());
        assertEquals("java.lang.String", info.getType());
        assertTrue(info.isReadable());
        assertFalse(info.isWritable());
      }
    }
    assertEquals(2, counter);

    // check the single operation

    assertNotNull(beanInfo.getOperations());
    assertEquals(3, beanInfo.getOperations().length);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributeGetterNameNotCaptial() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeGetterNameNotCapital(), null, "someName");

  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributePOJONamingNoGetter() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeNameNoGetterSetter(), null, "someName");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributeSetterNameNotCaptial() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeSetterNameNotCapital(), null, "someName");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributeHavingParameter() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeGetterHavinParameter(), null, "someName");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributeSetterHavingResult() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeSetterHavinReturn(), null, "someName");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributeVoid() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeVoid(), null, "someName");
  }

  @Test
  public void testInherited() throws JMException {
    ModelMBeanInfo beanInfo = mbeanInfoAssembler.getMBeanInfo(new BadInherited(), null, "someName");
    assertNotNull(beanInfo);
    assertNotNull(beanInfo.getAttributes());
    assertEquals(2, beanInfo.getAttributes().length);
    assertNotNull(beanInfo.getOperations());
    assertEquals(3, beanInfo.getOperations().length);

  }

}
