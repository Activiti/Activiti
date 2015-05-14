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

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceDiagramResourceTest extends BaseSpringRestTestCase {

  @Deployment
  public void testGetProcessDiagram() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleProcess");
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_DIAGRAM, processInstance.getId())), HttpStatus.SC_OK);
    assertNotNull(response.getEntity().getContent());
    assertEquals("image/png", response.getEntity().getContentType().getValue());
    closeResponse(response);
  }
  
  @Deployment
  public void testGetProcessDiagramWithoutDiagram() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_DIAGRAM, processInstance.getId())), HttpStatus.SC_BAD_REQUEST));
  }
  
  /**
   * Test getting an unexisting process instance.
   */
  public void testGetUnexistingProcessInstance() {
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_DIAGRAM, "unexistingpi")), HttpStatus.SC_NOT_FOUND));
  }
}
