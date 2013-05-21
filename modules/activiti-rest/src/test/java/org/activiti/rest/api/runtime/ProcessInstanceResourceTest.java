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

package org.activiti.rest.api.runtime;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.activiti.rest.api.RestUrls;

/**
 * Test for all REST-operations related to a single Process instance resource.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceResourceTest extends BaseRestTestCase {

  /**
   * Test getting a single process instance.
   */
  @Deployment(resources = {"org/activiti/rest/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testGetProcessInstance() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey");
    String id = processInstance.getId();
    runtimeService.addUserIdentityLink(id, "kermit", "whatever");
    
    // Test without any parameters
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION);
    assertResultsPresentInDataResponse(url, id);
    
    
    // Process instance id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?id=" + id;
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?id=anotherId";
    assertResultsPresentInDataResponse(url);
    
    // Process instance business key
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?businessKey=myBusinessKey";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?businessKey=anotherBusinessKey";
    assertResultsPresentInDataResponse(url);
    
    // Process definition key
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?processDefinitionKey=processOne";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?processDefinitionKey=processTwo";
    assertResultsPresentInDataResponse(url);
    
    // Process definition id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?processDefinitionId=" + processInstance.getProcessDefinitionId();
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?processDefinitionId=anotherId";
    assertResultsPresentInDataResponse(url);
    
     // Involved user
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?involvedUser=kermit";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?involvedUser=gonzo";
    assertResultsPresentInDataResponse(url);
    
    // Active process
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?suspended=false";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?suspended=true";
    assertResultsPresentInDataResponse(url);
    
    // Suspended process
    runtimeService.suspendProcessInstanceById(id);
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?suspended=true";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?suspended=false";
    assertResultsPresentInDataResponse(url);
    runtimeService.activateProcessInstanceById(id);
    
    // Complete first task in the process to have a subprocess created
    taskService.complete(taskService.createTaskQuery().processInstanceId(id).singleResult().getId());
    
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().superProcessInstanceId(id).singleResult();
    assertNotNull(subProcess);
    
    // Super-process instance id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?superProcessInstanceId=" + id;
    assertResultsPresentInDataResponse(url, subProcess.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?superProcessInstanceId=anotherId";
    assertResultsPresentInDataResponse(url);
    
    // Sub-process instance id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?subProcessInstanceId=" + subProcess.getId();
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?subProcessInstanceId=anotherId";
    assertResultsPresentInDataResponse(url);
    
  }
}
