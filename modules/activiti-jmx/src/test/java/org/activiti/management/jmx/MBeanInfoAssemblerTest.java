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

import org.activiti.management.jmx.mbeans.BadAttributeNameNotCaptal;
import org.activiti.management.jmx.mbeans.BadAttributeVoid;
import org.activiti.management.jmx.mbeans.NotManagedMBean;
import org.activiti.management.jmx.mbeans.TestMbean;
import org.junit.Test;

public class MBeanInfoAssemblerTest {

  TestMbean testMbean = new TestMbean();
  MBeanInfoAssembler mbeanInfoAssembler = new MBeanInfoAssembler();

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
    assertEquals(1, beanInfo.getOperations().length);
    MBeanOperationInfo operation = beanInfo.getOperations()[0];
    
      
    assertEquals("getTestOperation", operation.getName());
    assertEquals("test operation description", operation.getDescription());
    assertEquals("void", operation.getReturnType());


  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributePOJONamingNotCaptial() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeNameNotCaptal(), null, "someName");

  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributePOJONamingNoGetter() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeNameNotCaptal(), null, "someName");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAttributeVoid() throws JMException {
    mbeanInfoAssembler.getMBeanInfo(new BadAttributeVoid(), null, "someName");
  }

}
