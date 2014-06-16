
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

package org.activiti.examples.bpmn.authorization;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.test.Deployment;

/**
 * @author Saeid Mirzaei
 * @author Tijs Rademakers
 */

public class StartAuthorizationTest extends PluggableActivitiTestCase {

  IdentityService identityService;

  User userInGroup1;
  User userInGroup2;
  User userInGroup3;

  Group group1;
  Group group2;
  Group group3;

  protected void setUpUsersAndGroups() throws Exception {

    identityService = processEngine.getIdentityService();

    identityService.saveUser(identityService.newUser("user1"));
    identityService.saveUser(identityService.newUser("user2"));
    identityService.saveUser(identityService.newUser("user3"));

    // create users
    userInGroup1 = identityService.newUser("userInGroup1");
    identityService.saveUser(userInGroup1);

    userInGroup2 = identityService.newUser("userInGroup2");
    identityService.saveUser(userInGroup2);

    userInGroup3 = identityService.newUser("userInGroup3");
    identityService.saveUser(userInGroup3);

    // create groups
    group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    group2 = identityService.newGroup("group2");
    identityService.saveGroup(group2);

    group3 = identityService.newGroup("group3");
    identityService.saveGroup(group3);

    // relate users to groups
    identityService.createMembership(userInGroup1.getId(), group1.getId());
    identityService.createMembership(userInGroup2.getId(), group2.getId());
    identityService.createMembership(userInGroup3.getId(), group3.getId());
  }

  protected void tearDownUsersAndGroups() throws Exception {
    identityService.deleteMembership(userInGroup1.getId(), group1.getId());
    identityService.deleteMembership(userInGroup2.getId(), group2.getId());
    identityService.deleteMembership(userInGroup3.getId(), group3.getId());

    identityService.deleteGroup(group1.getId());
    identityService.deleteGroup(group2.getId());
    identityService.deleteGroup(group3.getId());

    identityService.deleteUser(userInGroup1.getId());
    identityService.deleteUser(userInGroup2.getId());
    identityService.deleteUser(userInGroup3.getId());
    
    identityService.deleteUser("user1");
    identityService.deleteUser("user2");
    identityService.deleteUser("user3");
  }
  
  @Deployment
  public void testIdentityLinks() throws Exception {
    
    setUpUsersAndGroups();
    
    try {
      ProcessDefinition latestProcessDef = repositoryService
          .createProcessDefinitionQuery().processDefinitionKey("process1")
          .singleResult();
      assertNotNull(latestProcessDef);
      List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(0, links.size());
      
      latestProcessDef = repositoryService
          .createProcessDefinitionQuery().processDefinitionKey("process2")
          .singleResult();
      assertNotNull(latestProcessDef);
      links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(2, links.size());
      assertEquals(true, containsUserOrGroup("user1", null, links));
      assertEquals(true, containsUserOrGroup("user2", null, links));
      
      latestProcessDef = repositoryService
          .createProcessDefinitionQuery().processDefinitionKey("process3")
          .singleResult();
      assertNotNull(latestProcessDef);
      links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(1, links.size());
      assertEquals("user1", links.get(0).getUserId());
      
      latestProcessDef = repositoryService
          .createProcessDefinitionQuery().processDefinitionKey("process4")
          .singleResult();
      assertNotNull(latestProcessDef);
      links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(4, links.size());
      assertEquals(true, containsUserOrGroup("userInGroup2", null, links));
      assertEquals(true, containsUserOrGroup(null, "group1", links));
      assertEquals(true, containsUserOrGroup(null, "group2", links));
      assertEquals(true, containsUserOrGroup(null, "group3", links));
      
    } finally {
      tearDownUsersAndGroups();
    }
  }
  
  @Deployment
  public void testAddAndRemoveIdentityLinks() throws Exception {
    
    setUpUsersAndGroups();
    
    try {
      ProcessDefinition latestProcessDef = repositoryService
          .createProcessDefinitionQuery().processDefinitionKey("potentialStarterNoDefinition")
          .singleResult();
      assertNotNull(latestProcessDef);
      List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(0, links.size());
      
      repositoryService.addCandidateStarterGroup(latestProcessDef.getId(), "group1");
      links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(1, links.size());
      assertEquals("group1", links.get(0).getGroupId());
      
      repositoryService.addCandidateStarterUser(latestProcessDef.getId(), "user1");
      links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(2, links.size());
      assertEquals(true, containsUserOrGroup(null, "group1", links));
      assertEquals(true, containsUserOrGroup("user1", null, links));
      
      repositoryService.deleteCandidateStarterGroup(latestProcessDef.getId(), "nonexisting");
      links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(2, links.size());
      
      repositoryService.deleteCandidateStarterGroup(latestProcessDef.getId(), "group1");
      links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(1, links.size());
      assertEquals("user1", links.get(0).getUserId());
      
      repositoryService.deleteCandidateStarterUser(latestProcessDef.getId(), "user1");
      links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
      assertEquals(0, links.size());
      
    } finally {
      tearDownUsersAndGroups();
    }
  }
  
  private boolean containsUserOrGroup(String userId, String groupId, List<IdentityLink> links) {
    boolean found = false;
    for (IdentityLink identityLink : links) {
      if(userId != null && userId.equals(identityLink.getUserId())) {
        found = true;
        break;
      } else if(groupId != null && groupId.equals(identityLink.getGroupId())) {
        found = true;
        break;
      }
    }
    return found;
  }

  @Deployment
  public void testPotentialStarter() throws Exception {
    // first check an unauthorized user. An exception is expected

    setUpUsersAndGroups();

    try {
    
	    // Authentication should not be done. So an unidentified user should also be able to start the process
	    identityService.setAuthenticatedUserId("unauthorizedUser");
	    try {
	      runtimeService.startProcessInstanceByKey("potentialStarter");
	
	    }  catch (Exception e) {
	      fail("No StartAuthorizationException expected, " + e.getClass().getName() + " caught.");
	    }
	
	    // check with an authorized user obviously it should be no problem starting the process
	    identityService.setAuthenticatedUserId("user1");
	    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("potentialStarter");
	    assertProcessEnded(processInstance.getId());
	    assertTrue(processInstance.isEnded());
	        
	    //check extensionElements with : <formalExpression>group2, group(group3), user(user3)</formalExpression>
	    ProcessDefinition potentialStarter = repositoryService.createProcessDefinitionQuery().processDefinitionKey("potentialStarter").startableByUser("user1").latestVersion().singleResult();
	    assertNotNull(potentialStarter);
	    
	    potentialStarter = repositoryService.createProcessDefinitionQuery().processDefinitionKey("potentialStarter").startableByUser("user3").latestVersion().singleResult();
	    assertNotNull(potentialStarter);
	    
	    potentialStarter = repositoryService.createProcessDefinitionQuery().processDefinitionKey("potentialStarter").startableByUser("userInGroup2").latestVersion().singleResult();
	    assertNotNull(potentialStarter);
	    
	    potentialStarter = repositoryService.createProcessDefinitionQuery().processDefinitionKey("potentialStarter").startableByUser("userInGroup3").latestVersion().singleResult();
	    assertNotNull(potentialStarter);
    } finally {

      tearDownUsersAndGroups();
    }
  }

  /*
   * if there is no security definition, then user authorization check is not
   * done. This ensures backward compatibility
   */
  @Deployment
  public void testPotentialStarterNoDefinition() throws Exception {
    identityService = processEngine.getIdentityService();

    identityService.setAuthenticatedUserId("someOneFromMars");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("potentialStarterNoDefinition");
    assertNotNull(processInstance.getId());
    assertProcessEnded(processInstance.getId());
    assertTrue(processInstance.isEnded());
  }
  
  // this test checks the list without user constraint
  @Deployment
	public void testProcessDefinitionList() throws Exception {
	  
    setUpUsersAndGroups();
    try {
      
      // Process 1 has no potential starters
      ProcessDefinition latestProcessDef = repositoryService
              .createProcessDefinitionQuery().processDefinitionKey("process1")
              .singleResult();      
      List<User> authorizedUsers = identityService.createUserQuery().potentialStarter(latestProcessDef.getId()).list();
      assertEquals(0, authorizedUsers.size());

      // user1 and user2 are potential Startes of Process2
      latestProcessDef = repositoryService
              .createProcessDefinitionQuery().processDefinitionKey("process2")
              .singleResult();      
      authorizedUsers =  identityService.createUserQuery().potentialStarter(latestProcessDef.getId()).orderByUserId().asc().list();
      assertEquals(2, authorizedUsers.size());
      assertEquals("user1", authorizedUsers.get(0).getId());
      assertEquals("user2", authorizedUsers.get(1).getId());

      // Process 2 has no potential starter groups
      latestProcessDef = repositoryService
              .createProcessDefinitionQuery().processDefinitionKey("process2")
              .singleResult();      
      List<Group> authorizedGroups = identityService.createGroupQuery().potentialStarter(latestProcessDef.getId()).list();
      assertEquals(0, authorizedGroups.size());
      
      // Process 3 has 3 groups as authorized starter groups
      latestProcessDef = repositoryService
              .createProcessDefinitionQuery().processDefinitionKey("process4")
              .singleResult();      
      authorizedGroups = identityService.createGroupQuery().potentialStarter(latestProcessDef.getId()).orderByGroupId().asc().list();
      assertEquals(3, authorizedGroups.size());
      assertEquals("group1", authorizedGroups.get(0).getId());
      assertEquals("group2", authorizedGroups.get(1).getId());
      assertEquals("group3", authorizedGroups.get(2).getId());

      // do not mention user, all processes should be selected
      List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
          .orderByProcessDefinitionName().asc().list();

      assertEquals(4, processDefinitions.size());

      assertEquals("process1", processDefinitions.get(0).getKey());
      assertEquals("process2", processDefinitions.get(1).getKey());
      assertEquals("process3", processDefinitions.get(2).getKey());
      assertEquals("process4", processDefinitions.get(3).getKey());

      // check user1, process3 has "user1" as only authorized starter, and
      // process2 has two authorized starters, of which one is "user1"
      processDefinitions = repositoryService.createProcessDefinitionQuery()
          .orderByProcessDefinitionName().asc().startableByUser("user1").list();

      assertEquals(2, processDefinitions.size());
      assertEquals("process2", processDefinitions.get(0).getKey());
      assertEquals("process3", processDefinitions.get(1).getKey());


      // "user2" can only start process2
      processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("user2").list();

      assertEquals(1, processDefinitions.size());
      assertEquals("process2", processDefinitions.get(0).getKey());

      // no process could be started with "user4"
      processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("user4").list();
      assertEquals(0, processDefinitions.size());

      // "userInGroup3" is in "group3" and can start only process4 via group authorization
      processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("userInGroup3").list();
      assertEquals(1, processDefinitions.size());
      assertEquals("process4", processDefinitions.get(0).getKey());

      // "userInGroup2" can start process4, via both user and group authorizations
      // but we have to be sure that process4 appears only once
      processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("userInGroup2").list();
      assertEquals(1, processDefinitions.size());
      assertEquals("process4", processDefinitions.get(0).getKey());
    
    } finally {
      tearDownUsersAndGroups();
    }
  }

}