package org.activiti.spring.test.transaction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Ravi Tadela
 */
@ContextConfiguration("classpath:org/activiti/spring/test/transaction/SpringIdentityServiceTest-context.xml")
public class SpringIdentityServiceTest extends SpringActivitiTestCase {


  @Deployment
  public void testCreateUserInTaskListener() {

	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("sampleProcess1");

	assertThat(processInstance, is(notNullValue()));
	Task task = taskService.createTaskQuery().singleResult();

	taskService.complete(task.getId());

	User user = identityService.createUserQuery().singleResult();
	assertNotNull(user);
	assertEquals("User1", user.getId());

	identityService.deleteUser("User1");

	assertEquals(0, identityService.createUserQuery().list().size());

  }


  @Deployment
  public void testCreateMembershipsInTaskListener() {

	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("sampleProcess2");

	assertThat(processInstance, is(notNullValue()));
	Task task = taskService.createTaskQuery().singleResult();

	taskService.complete(task.getId());
	assertThat(identityService.createGroupQuery().groupId("testgroup1").singleResult(), is(notNullValue()));

	assertThat(identityService.createUserQuery().userId("user1").singleResult(), is(notNullValue()));
	assertThat(identityService.createUserQuery().userId("user2").singleResult(), is(notNullValue()));
	assertThat(identityService.createUserQuery().userId("user3").singleResult(), is(notNullValue()));

	assertThat(identityService.createGroupQuery().groupMember("user1").singleResult(), is(notNullValue()));
	assertThat(identityService.createGroupQuery().groupMember("user2").singleResult(), is(notNullValue()));
	assertThat(identityService.createGroupQuery().groupMember("user3").singleResult(), is(notNullValue()));
  }



  public static class TestCreateUserTaskListener implements TaskListener {

	@Autowired
	private IdentityService identityService;

	@Override
	public void notify(DelegateTask delegateTask) {

	  User user = identityService.newUser("User1");
	  user.setFirstName("User1");
	  user.setLastName("Created");
	  user.setEmail("User1@activiti.com");
	  user.setPassword("User1");
	  identityService.saveUser(user);
	}

  }

  public static class TestCreateMembershipTaskListener implements TaskListener {

	@Autowired
	private IdentityService identityService;

	@Override
	public void notify(DelegateTask delegateTask) {
	  //create group
	  Group group = identityService.newGroup("testgroup1");
	  identityService.saveGroup(group);

	  //create user1
	  User user1 = identityService.newUser("user1");
	  user1.setEmail("user1@activiti.com");
	  user1.setPassword("user1");
	  user1.setFirstName("User1");
	  user1.setLastName("Created");
	  identityService.saveUser(user1);

	  //create user2
	  User user2 = identityService.newUser("user2");
	  user2.setEmail("user1@activiti.com");
	  user2.setPassword("user2");
	  user2.setFirstName("User2");
	  user2.setLastName("Created");
	  identityService.saveUser(user2);

	  //create user3
	  User user3 = identityService.newUser("user3");
	  user3.setEmail("user3@activiti.com");
	  user3.setPassword("user3");
	  user3.setFirstName("User3");
	  user3.setLastName("Created");
	  identityService.saveUser(user3);

	  // create memberships
	  identityService.createMembership(user1.getId(), group.getId());
	  identityService.createMembership(user2.getId(), group.getId());
	  identityService.createMembership(user3.getId(), group.getId());
	}

  }

  @Override
  protected void tearDown() throws Exception {

	List<Group> allGroups = identityService.createGroupQuery().list();
	for (Group group : allGroups) {
	  List<User> members = identityService.createUserQuery().memberOfGroup(group.getId()).list();
	  for (User member : members) {
		identityService.deleteMembership(member.getId(), group.getId());
	  }
	  identityService.deleteGroup(group.getId());
	}

	List<User> allUsers = identityService.createUserQuery().list();
	for (User user : allUsers) {
	  identityService.deleteUser(user.getId());
	}

	super.tearDown();
  }


}
