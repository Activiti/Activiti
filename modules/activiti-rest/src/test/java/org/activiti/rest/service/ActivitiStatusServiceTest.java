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

package org.activiti.rest.service;

import junit.framework.TestCase;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.rest.common.application.ActivitiStatusService;
import org.restlet.data.Status;
import org.restlet.service.StatusService;


/**
 * @author Frederik Heremans
 */
public class ActivitiStatusServiceTest extends TestCase {

  private StatusService statusService;
  
  @Override
  protected void setUp() throws Exception {
    statusService = new ActivitiStatusService();
  }
  
  public void testNewlineInMessage() throws Exception {
    Status status = statusService.getStatus(new ActivitiException("This is a\n newline"), null);
    
    assertNotNull(status);
    assertEquals("This is a  newline", status.getReasonPhrase());
  }
  
  public void test404WhenNotFound() throws Exception {
    Status status = statusService.getStatus(new ActivitiObjectNotFoundException(String.class), null);
    
    assertNotNull(status);
    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, status);
  }
  
  public void test400WhenIllegalArgument() throws Exception {
    Status status = statusService.getStatus(new ActivitiIllegalArgumentException("testing"), null);
    assertNotNull(status);
    assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, status);
  }
  
  public void test409WhenConflict() throws Exception {
    Status status = statusService.getStatus(new ActivitiTaskAlreadyClaimedException("id", "assignee"), null);
    assertNotNull(status);
    assertEquals(Status.CLIENT_ERROR_CONFLICT, status);
    
    status = statusService.getStatus(new ActivitiOptimisticLockingException("testing"), null);
    assertNotNull(status);
    assertEquals(Status.CLIENT_ERROR_CONFLICT, status);
  }
}
