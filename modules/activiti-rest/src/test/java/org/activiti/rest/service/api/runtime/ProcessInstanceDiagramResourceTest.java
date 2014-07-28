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
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceDiagramResourceTest extends BaseRestTestCase {

  @Deployment
  public void testGetProcessDiagram() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleProcess");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_DIAGRAM, processInstance.getId()));
    
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    assertTrue(response.isAvailable());
  }
  
  @Deployment
  public void testGetProcessDiagramWithoutDiagram() throws Exception {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_DIAGRAM, processInstance.getId()));
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Process instance with id '" + processInstance.getId() + "' has no graphical notation defined.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test getting an unexisting process instance.
   */
  public void testGetUnexistingProcessInstance() {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_DIAGRAM, "unexistingpi"));
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a process instance with id 'unexistingpi'.", expected.getStatus().getDescription());
    }
  }
}
