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
package org.activiti.engine.test.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * @author Esteban Robles Luna
 */
public class WebServiceSimplisticTest extends AbstractWebServiceTaskTest {

  protected boolean isValidating() {
    return false;
  }
  
  @Deployment
  public void testWebServiceInvocationWithSimplisticDataFlow() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("PrefixVariable", "The counter has the value ");
    variables.put("SuffixVariable", ". Good news");

    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("webServiceInvocationWithSimplisticDataFlow", variables);
    waitForJobExecutorToProcessAllJobs(10000L, 250L);

    String response = (String) processEngine.getRuntimeService().getVariable(instance.getId(), "OutputVariable");
    assertEquals("The counter has the value -1. Good news", response);
  }

  @Deployment
  public void testWebResponseNoName() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("PrefixVariable", "The counter has the value ");
    variables.put("SuffixVariable", ". Good news (NO NAME)");

    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("webServiceInvocationWithSimplisticDataFlow", variables);
    waitForJobExecutorToProcessAllJobs(10000L, 250L);

    String response = (String) processEngine.getRuntimeService().getVariable(instance.getId(), "OutputVariable");
    assertEquals("The counter has the value -1. Good news (NO NAME)", response);
  }

  @Deployment
  public void testWebResponseKeywordName() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("PrefixVariable", "The counter has the value ");
    variables.put("SuffixVariable", ". Good news Keyword");

    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("webServiceInvocationWithSimplisticDataFlow", variables);
    waitForJobExecutorToProcessAllJobs(10000L, 250L);

    String response = (String) processEngine.getRuntimeService().getVariable(instance.getId(), "OutputVariable");
    assertEquals("The counter has the value -1. Good news Keyword", response);
  }
}
