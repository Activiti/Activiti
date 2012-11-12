package org.activiti.camel;

import org.apache.camel.Exchange;

public class SleepBean {

  public void sleep(String body, Exchange exchange) throws Exception {
    Thread.sleep(1500);
  }
}
