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

package org.activiti.camel.examples.initiatorCamelCall;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:camel-activiti-context.xml")
public class InitiatorCamelCallTest extends SpringActivitiTestCase {
   
  @Deployment
  public void testInitiatorCamelCall() throws Exception {
    CamelContext ctx = applicationContext.getBean(CamelContext.class);
    ProducerTemplate tpl = ctx.createProducerTemplate();
    String body = "body text";
    String instanceId = (String) tpl.requestBody("direct:startWithInitiatorHeader", body);

    String initiator = (String) runtimeService.getVariable(instanceId, "initiator");
    assertEquals("kermit", initiator);
    
    Object camelInitiatorHeader = runtimeService.getVariable(instanceId, "CamelProcessInitiatorHeader");
    assertNull(camelInitiatorHeader);
  }

}
