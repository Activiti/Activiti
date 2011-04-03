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
package org.activiti.cdi.impl.context;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.CdiActivitiTestCase;
import org.activiti.cdi.test.beans.CreditCard;
import org.activiti.cdi.test.beans.ProcessScopedMessageBean;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.test.Deployment;

/**
 * 
 * @author Daniel Meyer
 */
public class BusinessProcessContextTest extends CdiActivitiTestCase {

  @Deployment
  public void testResolution() throws Exception {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    businessProcess.startProcessByKey("testResolution").getId();

    assertNotNull(getBeanInstance(CreditCard.class));    
  }

  // no @Deployment for this test
  public void testResolutionBeforeProcessStart() throws Exception {
    // assert that @BusinessProcessScoped beans can be resolved in the absence of an underlying process instance:
    assertNotNull(getBeanInstance(CreditCard.class));
  }

  @Deployment
  public void testConversationalBeanStoreFlush() throws Exception {
    
    getBeanInstance(BusinessProcess.class).setProcessVariable("testVariable", "testValue");
    String pid =  getBeanInstance(BusinessProcess.class).startProcessByKey("testConversationalBeanStoreFlush").getId();

    endConversationAndBeginNew(pid); ///////////////////////////////////////////// 2nd Conversation

    // assert that the variable assigned on the businessProcess bean is flushed 
    assertEquals("testValue", runtimeService.getVariable(pid, "testVariable"));

    // assert that the value set to the message bean in the first service task is flushed
    assertEquals("Hello from Activiti", getBeanInstance(ProcessScopedMessageBean.class).getMessage());

    // complete the task to allow the process instance to terminate
    getBeanInstance(BusinessProcess.class).completeTask();
  }

  @Deployment
  public void testChangeProcessScopedBeanProperty() throws Exception {
    
    // resolve the creditcard bean (@BusinessProcessScoped) and set a value:
    getBeanInstance(CreditCard.class).setCreditcardNumber("123");
    String pid = getBeanInstance(BusinessProcess.class).startProcessByKey("testConversationalBeanStoreFlush").getId();

    endConversationAndBeginNew(pid); ///////////////////////////////////////////// 2nd Conversation
        
    // assert that the value of creditCardNumber is '123'
    assertEquals("123", getBeanInstance(CreditCard.class).getCreditcardNumber());
    // set a different value:
    getBeanInstance(CreditCard.class).setCreditcardNumber("321");
    // complete the task
    getBeanInstance(BusinessProcess.class).completeTask();
    
    endConversationAndBeginNew(pid); ///////////////////////////////////////////// 3rd Conversation

    // now assert that the value of creditcard is "321":
    assertEquals("321", getBeanInstance(CreditCard.class).getCreditcardNumber());
    
    // complete the task to allow the process instance to terminate
    getBeanInstance(BusinessProcess.class).completeTask();
    
  }
    
}
