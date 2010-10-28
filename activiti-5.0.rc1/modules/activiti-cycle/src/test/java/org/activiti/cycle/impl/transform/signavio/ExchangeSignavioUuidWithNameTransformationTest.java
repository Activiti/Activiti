package org.activiti.cycle.impl.transform.signavio;

import static org.junit.Assert.*;

import org.junit.Test;


public class ExchangeSignavioUuidWithNameTransformationTest {

  @Test
  public void testAdjustNamesForEngine() {
    String name = "Send rejection e-mail";
    String actual = ExchangeSignavioUuidWithNameTransformation.adjustNamesForEngine(name);
    assertEquals("Send_rejection_e-mail", actual);
  }

}
