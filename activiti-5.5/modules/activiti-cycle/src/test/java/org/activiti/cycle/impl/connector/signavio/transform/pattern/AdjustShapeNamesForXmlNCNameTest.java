package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import static org.junit.Assert.assertEquals;

import org.activiti.cycle.impl.ActivitiCycleTest;
import org.junit.Test;


public class AdjustShapeNamesForXmlNCNameTest extends ActivitiCycleTest {

  @Test
  public void testAdjustNamesForEngine() {
    assertEquals("Send_rejection_e-mail", AdjustShapeNamesForXmlNCName.adjustForXmlNCName("Send rejection e-mail"));
    assertEquals("______________________________-_", AdjustShapeNamesForXmlNCName.adjustForXmlNCName("<!\"§$%&/()=?{[]}\\´`.,_;#+*~> :-)"));
  }

}
