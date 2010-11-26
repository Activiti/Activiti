package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ExchangeSignavioUuidWithNameTransformationTest {

  @Test
  public void testAdjustNamesForEngine() {
    assertEquals("Send_rejection_e-mail", ExchangeSignavioUuidWithNameTransformation.adjustNamesForEngine("Send rejection e-mail"));
    assertEquals("_15__everything_mine__-_", ExchangeSignavioUuidWithNameTransformation.adjustNamesForEngine("§15: everything mine :-)"));
  }

}
