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

package org.activiti.camel.route;

import org.apache.camel.builder.RouteBuilder;

public class AsyncCamelRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {

    from("activiti:asyncCamelProcess:serviceTaskAsync1").setHeader("destination", constant("activiti:asyncCamelProcess:receive1")).to("seda:asyncQueue");
    from("seda:asyncQueue").to("bean:sleepBean?method=sleep").to("seda:receiveQueue");
    
    from("activiti:asyncCamelProcess:serviceTaskAsync2").setHeader("destination", constant("activiti:asyncCamelProcess:receive2")).to("seda:asyncQueue2");
    from("seda:asyncQueue2").to("bean:sleepBean?method=sleep").to("seda:receiveQueue");
    
    from("seda:receiveQueue").recipientList(header("destination"));
  }
}
