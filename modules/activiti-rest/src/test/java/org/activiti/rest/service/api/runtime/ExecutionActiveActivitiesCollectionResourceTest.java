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

package org.activiti.rest.service.api.runtime;

import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * @author Frederik Heremans
 */
public class ExecutionActiveActivitiesCollectionResourceTest extends BaseSpringRestTestCase {

  @Deployment
  public void testGetActivities() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_ACTIVITIES_COLLECTION, processInstance.getId())), HttpStatus.SC_OK);
    
    // Check resulting instance
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(2, responseNode.size());
    
    Set<String> states = new HashSet<String>();
    states.add(responseNode.get(0).textValue());
    states.add(responseNode.get(1).textValue());
    
    assertTrue(states.contains("waitState"));
    assertTrue(states.contains("anotherWaitState"));
  }
}
