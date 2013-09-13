package org.activiti.camel.route;

import org.apache.camel.builder.RouteBuilder;


public class AsyncPingRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("activiti:asyncPingProcess:serviceAsyncPing").to("activiti:asyncPingProcess:receiveAsyncPing");
  }

}
