package org.activiti.camel.util;

import org.apache.camel.Exchange;

public class SleepBean {

  public void sleep(String body, Exchange exchange) throws Exception {
    Thread.sleep(500);
  }
}
