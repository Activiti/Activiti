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
package org.activiti.engine.test.api.event;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiMembershipEvent;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

/**
 * Test case for all {@link ActivitiEvent}s related to groups.
 * 
 * @author Frederik Heremans
 */
public class GroupEventsTest extends PluggableActivitiTestCase {

	private TestActivitiEntityEventListener listener;

	/**
	 * Test create, update and delete events of Groups.
	 */
	public void testGroupEntityEvents() throws Exception {
		Group group = null;
		try {
			group = identityService.newGroup("fred");
			group.setName("name");
			group.setType("type");
			identityService.saveGroup(group);

			assertEquals(2, listener.getEventsReceived().size());
			ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertTrue(event.getEntity() instanceof Group);
			Group groupFromEvent = (Group) event.getEntity();
			assertEquals("fred", groupFromEvent.getId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getExecutionId());
			assertNull(event.getProcessInstanceId());
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
			listener.clearEventsReceived();

			// Update Group
			group.setName("Another name");
			identityService.saveGroup(group);
			assertEquals(1, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
			assertTrue(event.getEntity() instanceof Group);
			groupFromEvent = (Group) event.getEntity();
			assertEquals("fred", groupFromEvent.getId());
			assertEquals("Another name", groupFromEvent.getName());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getExecutionId());
			assertNull(event.getProcessInstanceId());
			listener.clearEventsReceived();

			// Delete Group
			identityService.deleteGroup(group.getId());
			
			assertEquals(1, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertTrue(event.getEntity() instanceof Group);
			groupFromEvent = (Group) event.getEntity();
			assertEquals("fred", groupFromEvent.getId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getExecutionId());
			assertNull(event.getProcessInstanceId());
			listener.clearEventsReceived();

		} finally {
			if (group != null && group.getId() != null) {
				identityService.deleteGroup(group.getId());
			}
		}
	}

	/**
	 * Test create, update and delete events of Groups.
	 */
	public void testGroupMembershipEvents() throws Exception {
		TestActivitiEventListener membershipListener = new TestActivitiEventListener();
		processEngineConfiguration.getEventDispatcher().addEventListener(membershipListener);
		
		User user = null;
		Group group = null;
		try {
			user = identityService.newUser("kermit");
			identityService.saveUser(user);
			
			group = identityService.newGroup("sales");
			identityService.saveGroup(group);
			
			// Add membership
			membershipListener.clearEventsReceived();
			identityService.createMembership("kermit", "sales");
			assertEquals(1, membershipListener.getEventsReceived().size());
			assertTrue(membershipListener.getEventsReceived().get(0) instanceof ActivitiMembershipEvent);
			ActivitiMembershipEvent event = (ActivitiMembershipEvent) membershipListener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.MEMBERSHIP_CREATED, event.getType());
			assertEquals("sales", event.getGroupId());
			assertEquals("kermit", event.getUserId());
			assertNull(event.getExecutionId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			membershipListener.clearEventsReceived();
			
			// Delete membership
			identityService.deleteMembership("kermit", "sales");
			assertEquals(1, membershipListener.getEventsReceived().size());
			assertTrue(membershipListener.getEventsReceived().get(0) instanceof ActivitiMembershipEvent);
			event = (ActivitiMembershipEvent) membershipListener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.MEMBERSHIP_DELETED, event.getType());
			assertEquals("sales", event.getGroupId());
			assertEquals("kermit", event.getUserId());
			assertNull(event.getExecutionId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			membershipListener.clearEventsReceived();
			
			// Deleting group will dispatch an event, informing ALL memberships are deleted
			identityService.createMembership("kermit", "sales");
			membershipListener.clearEventsReceived();
			identityService.deleteGroup(group.getId());
			
			assertEquals(2, membershipListener.getEventsReceived().size());
			assertTrue(membershipListener.getEventsReceived().get(0) instanceof ActivitiMembershipEvent);
			event = (ActivitiMembershipEvent) membershipListener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.MEMBERSHIPS_DELETED, event.getType());
			assertEquals("sales", event.getGroupId());
			assertNull(event.getUserId());
			assertNull(event.getExecutionId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			membershipListener.clearEventsReceived();
		} finally {
			processEngineConfiguration.getEventDispatcher().removeEventListener(membershipListener);
			if(user != null) {
				identityService.deleteUser(user.getId());
			}
			if(group != null) {
				identityService.deleteGroup(group.getId());
			}
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		listener = new TestActivitiEntityEventListener(Group.class);
		processEngineConfiguration.getEventDispatcher().addEventListener(listener);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		if (listener != null) {
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
}
