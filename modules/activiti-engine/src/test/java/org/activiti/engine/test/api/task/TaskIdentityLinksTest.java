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

package org.activiti.engine.test.api.task;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public class TaskIdentityLinksTest extends PluggableActivitiTestCase {

  @Deployment(resources="org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCandidateUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");
    
    String taskId = taskService
      .createTaskQuery()
      .singleResult()
      .getId();
    
    taskService.addCandidateUser(taskId, "kermit");
    
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);
    
    assertNull(identityLink.getGroupId());
    assertEquals("kermit", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
    assertEquals(taskId, identityLink.getTaskId());
    
    assertEquals(1, identityLinks.size());

    taskService.deleteCandidateUser(taskId, "kermit");
    
    assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
  }

  @Deployment(resources="org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCandidateGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");
    
    String taskId = taskService
      .createTaskQuery()
      .singleResult()
      .getId();
    
    taskService.addCandidateGroup(taskId, "muppets");
    
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);
    
    assertEquals("muppets", identityLink.getGroupId());
    assertNull("kermit", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
    assertEquals(taskId, identityLink.getTaskId());
    
    assertEquals(1, identityLinks.size());
    
    Event taskEvent = taskService.getTaskEvents(taskId).get(0);
    assertEquals(Event.ACTION_ADD_IDENTITY_LINK, taskEvent.getAction());
    List<String> taskEventMessageParts = taskEvent.getMessageParts();
    assertNull(taskEventMessageParts.get(0));
    assertEquals("muppets", taskEventMessageParts.get(1));
    assertEquals(IdentityLinkType.CANDIDATE, taskEventMessageParts.get(2));

    taskService.deleteCandidateGroup(taskId, "muppets");
    
    assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
  }

  @Deployment(resources="org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCustomTypeUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");
    
    String taskId = taskService
      .createTaskQuery()
      .singleResult()
      .getId();
    
    taskService.addUserIdentityLink(taskId, "kermit", "interestee");
    
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);
    
    assertNull(identityLink.getGroupId());
    assertEquals("kermit", identityLink.getUserId());
    assertEquals("interestee", identityLink.getType());
    assertEquals(taskId, identityLink.getTaskId());
    
    assertEquals(1, identityLinks.size());

    taskService.deleteUserIdentityLink(taskId, "kermit", "interestee");
    
    assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
  }

  @Deployment(resources="org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCustomLinkGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");
    
    String taskId = taskService
      .createTaskQuery()
      .singleResult()
      .getId();
    
    taskService.addGroupIdentityLink(taskId, "muppets", "playing");
    
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);
    
    assertEquals("muppets", identityLink.getGroupId());
    assertNull("kermit", identityLink.getUserId());
    assertEquals("playing", identityLink.getType());
    assertEquals(taskId, identityLink.getTaskId());
    
    assertEquals(1, identityLinks.size());

    taskService.deleteGroupIdentityLink(taskId, "muppets", "playing");
    
    assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
  }

}
