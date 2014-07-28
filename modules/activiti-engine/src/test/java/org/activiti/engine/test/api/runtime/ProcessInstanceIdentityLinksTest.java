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

package org.activiti.engine.test.api.runtime;

import java.util.List;

import junit.framework.AssertionFailedError;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.test.Deployment;


/**
 * @author Wendel Kerr
 */
public class ProcessInstanceIdentityLinksTest extends PluggableActivitiTestCase {

  @Deployment(resources="org/activiti/engine/test/api/runtime/IdentityLinksProcess.bpmn20.xml")
  public void testParticipantUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");
    
    String processInstanceId = runtimeService
      .createProcessInstanceQuery()
      .singleResult()
      .getId();
    
    runtimeService.addParticipantUser(processInstanceId, "kermit");
    
    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    IdentityLink identityLink = identityLinks.get(0);
    
    assertNull(identityLink.getGroupId());
    assertEquals("kermit", identityLink.getUserId());
    assertEquals(IdentityLinkType.PARTICIPANT, identityLink.getType());
    assertEquals(processInstanceId, identityLink.getProcessInstanceId());
    
    assertEquals(1, identityLinks.size());

    runtimeService.deleteParticipantUser(processInstanceId, "kermit");
    
    assertEquals(0, runtimeService.getIdentityLinksForProcessInstance(processInstanceId).size());
  }

  @Deployment(resources="org/activiti/engine/test/api/runtime/IdentityLinksProcess.bpmn20.xml")
  public void testCandidateGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");
    
    String processInstanceId = runtimeService
      .createProcessInstanceQuery()
      .singleResult()
      .getId();
    
    runtimeService.addParticipantGroup(processInstanceId, "muppets");
    
    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    IdentityLink identityLink = identityLinks.get(0);
    
    assertEquals("muppets", identityLink.getGroupId());
    assertNull("kermit", identityLink.getUserId());
    assertEquals(IdentityLinkType.PARTICIPANT, identityLink.getType());
    assertEquals(processInstanceId, identityLink.getProcessInstanceId());
    
    assertEquals(1, identityLinks.size());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<Event> processInstanceEvents = runtimeService.getProcessInstanceEvents(processInstanceId);
      assertEquals(1, processInstanceEvents.size());
      Event processIsntanceEvent = processInstanceEvents.get(0);
      assertEquals(Event.ACTION_ADD_GROUP_LINK, processIsntanceEvent.getAction());
      List<String> processInstanceEventMessageParts = processIsntanceEvent.getMessageParts();
      assertEquals("muppets", processInstanceEventMessageParts.get(0));
      assertEquals(IdentityLinkType.PARTICIPANT, processInstanceEventMessageParts.get(1));
      assertEquals(2, processInstanceEventMessageParts.size());
    }
    
    runtimeService.deleteParticipantGroup(processInstanceId, "muppets");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<Event> processInstanceEvents = runtimeService.getProcessInstanceEvents(processInstanceId);
      Event processIsntanceEvent = findProcessInstanceEvent(processInstanceEvents, Event.ACTION_DELETE_GROUP_LINK);
      assertEquals(Event.ACTION_DELETE_GROUP_LINK, processIsntanceEvent.getAction());
      List<String> processInstanceEventMessageParts = processIsntanceEvent.getMessageParts();
      assertEquals("muppets", processInstanceEventMessageParts.get(0));
      assertEquals(IdentityLinkType.PARTICIPANT, processInstanceEventMessageParts.get(1));
      assertEquals(2, processInstanceEventMessageParts.size());
      assertEquals(2, processInstanceEvents.size());
    }

    assertEquals(0, runtimeService.getIdentityLinksForProcessInstance(processInstanceId).size());
  }

   private Event findProcessInstanceEvent(List<Event> processInstanceEvents, String action) {
    for (Event event: processInstanceEvents) {
      if (action.equals(event.getAction())) {
        return event;
      }
    }
    throw new AssertionFailedError("no process instance event found with action "+action);
  }

  @Deployment(resources="org/activiti/engine/test/api/runtime/IdentityLinksProcess.bpmn20.xml")
  public void testCustomTypeUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");
    
    String processInstanceId = runtimeService
      .createProcessInstanceQuery()
      .singleResult()
      .getId();
    
    runtimeService.addUserIdentityLink(processInstanceId, "kermit", "interestee");
    
    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    IdentityLink identityLink = identityLinks.get(0);
    
    assertNull(identityLink.getGroupId());
    assertEquals("kermit", identityLink.getUserId());
    assertEquals("interestee", identityLink.getType());
    assertEquals(processInstanceId, identityLink.getProcessInstanceId());
    
    assertEquals(1, identityLinks.size());

    runtimeService.deleteUserIdentityLink(processInstanceId, "kermit", "interestee");
    
    assertEquals(0, runtimeService.getIdentityLinksForProcessInstance(processInstanceId).size());
  }

  @Deployment(resources="org/activiti/engine/test/api/runtime/IdentityLinksProcess.bpmn20.xml")
  public void testCustomLinkGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");
    
    String processInstanceId = runtimeService
      .createProcessInstanceQuery()
      .singleResult()
      .getId();
    
    runtimeService.addGroupIdentityLink(processInstanceId, "muppets", "playing");
    
    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    IdentityLink identityLink = identityLinks.get(0);
    
    assertEquals("muppets", identityLink.getGroupId());
    assertNull("kermit", identityLink.getUserId());
    assertEquals("playing", identityLink.getType());
    assertEquals(processInstanceId, identityLink.getProcessInstanceId());
    
    assertEquals(1, identityLinks.size());

    runtimeService.deleteGroupIdentityLink(processInstanceId, "muppets", "playing");
    
    assertEquals(0, runtimeService.getIdentityLinksForProcessInstance(processInstanceId).size());
  }

}
