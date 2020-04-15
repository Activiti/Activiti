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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import junit.framework.AssertionFailedError;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.test.Deployment;

/**
 */
public class ProcessInstanceIdentityLinksTest extends PluggableActivitiTestCase {

  @Deployment(resources = "org/activiti/engine/test/api/runtime/IdentityLinksProcess.bpmn20.xml")
  public void testParticipantUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();

    runtimeService.addParticipantUser(processInstanceId, "kermit");

    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getGroupId()).isNull();
    assertThat(identityLink.getUserId()).isEqualTo("kermit");
    assertThat(identityLink.getType()).isEqualTo(IdentityLinkType.PARTICIPANT);
    assertThat(identityLink.getProcessInstanceId()).isEqualTo(processInstanceId);

    assertThat(identityLinks).hasSize(1);

    runtimeService.deleteParticipantUser(processInstanceId, "kermit");

    assertThat(runtimeService.getIdentityLinksForProcessInstance(processInstanceId)).hasSize(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/runtime/IdentityLinksProcess.bpmn20.xml")
  public void testCandidateGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();

    runtimeService.addParticipantGroup(processInstanceId, "muppets");

    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getGroupId()).isEqualTo("muppets");
    assertThat(identityLink.getUserId()).as("kermit").isNull();
    assertThat(identityLink.getType()).isEqualTo(IdentityLinkType.PARTICIPANT);
    assertThat(identityLink.getProcessInstanceId()).isEqualTo(processInstanceId);

    assertThat(identityLinks).hasSize(1);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<Event> processInstanceEvents = runtimeService.getProcessInstanceEvents(processInstanceId);
      assertThat(processInstanceEvents).hasSize(1);
      Event processInstanceEvent = processInstanceEvents.get(0);
      assertThat(processInstanceEvent.getAction()).isEqualTo(Event.ACTION_ADD_GROUP_LINK);
      List<String> processInstanceEventMessageParts = processInstanceEvent.getMessageParts();
      assertThat(processInstanceEventMessageParts.get(0)).isEqualTo("muppets");
      assertThat(processInstanceEventMessageParts.get(1)).isEqualTo(IdentityLinkType.PARTICIPANT);
      assertThat(processInstanceEventMessageParts).hasSize(2);
    }

    runtimeService.deleteParticipantGroup(processInstanceId, "muppets");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<Event> processInstanceEvents = runtimeService.getProcessInstanceEvents(processInstanceId);
      Event processIsntanceEvent = findProcessInstanceEvent(processInstanceEvents, Event.ACTION_DELETE_GROUP_LINK);
      assertThat(processIsntanceEvent.getAction()).isEqualTo(Event.ACTION_DELETE_GROUP_LINK);
      List<String> processInstanceEventMessageParts = processIsntanceEvent.getMessageParts();
      assertThat(processInstanceEventMessageParts.get(0)).isEqualTo("muppets");
      assertThat(processInstanceEventMessageParts.get(1)).isEqualTo(IdentityLinkType.PARTICIPANT);
      assertThat(processInstanceEventMessageParts).hasSize(2);
      assertThat(processInstanceEvents).hasSize(2);
    }

    assertThat(runtimeService.getIdentityLinksForProcessInstance(processInstanceId)).hasSize(0);
  }

  private Event findProcessInstanceEvent(List<Event> processInstanceEvents, String action) {
    for (Event event : processInstanceEvents) {
      if (action.equals(event.getAction())) {
        return event;
      }
    }
    throw new AssertionFailedError("no process instance event found with action " + action);
  }

  @Deployment(resources = "org/activiti/engine/test/api/runtime/IdentityLinksProcess.bpmn20.xml")
  public void testCustomTypeUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();

    runtimeService.addUserIdentityLink(processInstanceId, "kermit", "interestee");

    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getGroupId()).isNull();
    assertThat(identityLink.getUserId()).isEqualTo("kermit");
    assertThat(identityLink.getType()).isEqualTo("interestee");
    assertThat(identityLink.getProcessInstanceId()).isEqualTo(processInstanceId);

    assertThat(identityLinks).hasSize(1);

    runtimeService.deleteUserIdentityLink(processInstanceId, "kermit", "interestee");

    assertThat(runtimeService.getIdentityLinksForProcessInstance(processInstanceId)).hasSize(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/runtime/IdentityLinksProcess.bpmn20.xml")
  public void testCustomLinkGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();

    runtimeService.addGroupIdentityLink(processInstanceId, "muppets", "playing");

    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getGroupId()).isEqualTo("muppets");
    assertThat(identityLink.getUserId()).as("kermit").isNull();
    assertThat(identityLink.getType()).isEqualTo("playing");
    assertThat(identityLink.getProcessInstanceId()).isEqualTo(processInstanceId);

    assertThat(identityLinks).hasSize(1);

    runtimeService.deleteGroupIdentityLink(processInstanceId, "muppets", "playing");

    assertThat(runtimeService.getIdentityLinksForProcessInstance(processInstanceId)).hasSize(0);
  }

}
