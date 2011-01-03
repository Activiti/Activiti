package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class AdjustShapeNamesForXmlNCNameTest {

  @Test
  public void testAdjustNamesForEngine() {
    assertEquals("Send_rejection_e-mail", AdjustShapeNamesForXmlNCName.adjustForXmlNCName("Send rejection e-mail"));
    assertEquals("______________________________-_", AdjustShapeNamesForXmlNCName.adjustForXmlNCName("<!\"§$%&/()=?{[]}\\´`.,_;#+*~> :-)"));
  }

}
