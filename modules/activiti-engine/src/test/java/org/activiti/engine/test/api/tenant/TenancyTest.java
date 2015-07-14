package org.activiti.engine.test.api.tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Assert;

/**
 * A test case for the various implications of the tenancy support (tenant id column to entities + query support)
 * 
 * @author jbarrez
 */
public class TenancyTest extends PluggableActivitiTestCase {
	
	private static final String TEST_TENANT_ID = "myTenantId";
	
	private List<String> autoCleanedUpDeploymentIds = new ArrayList<String>();
	
	@Override
	protected void setUp() throws Exception {
	  super.setUp();
	  this.autoCleanedUpDeploymentIds.clear();
	}
	
	@Override
	protected void tearDown() throws Exception {
	  super.tearDown();
	  
	  if (!autoCleanedUpDeploymentIds.isEmpty()) {
	  	for (String deploymentId : autoCleanedUpDeploymentIds) {
	  		repositoryService.deleteDeployment(deploymentId, true);
	  	}
	  }
	}
	
	/**
	 * Deploys the one task process woth the test tenand id.
	 * 
	 * @return The process definition id of the deployed process definition.
	 */
	private String deployTestProcessWithTestTenant() {
	  return deployTestProcessWithTestTenant(TEST_TENANT_ID);
  }
	
	private String deployTestProcessWithTestTenant(String tenantId) {
	  String id = repositoryService.createDeployment()
			.addBpmnModel("testProcess.bpmn20.xml", createOneTaskTestProcess())
			.tenantId(tenantId)
			.deploy()
			.getId();
	  
	  autoCleanedUpDeploymentIds.add(id);
	  
	  return repositoryService.createProcessDefinitionQuery()
	  		.deploymentId(id)
	  		.singleResult()
	  		.getId();
  }
	
	
	
	private String deployTestProcessWithTwoTasksWithTestTenant() {
	  String id = repositoryService.createDeployment()
			.addBpmnModel("testProcess.bpmn20.xml", createTwoTasksTestProcess())
			.tenantId(TEST_TENANT_ID)
			.deploy()
			.getId();
	  
	  autoCleanedUpDeploymentIds.add(id);
	  
	  return repositoryService.createProcessDefinitionQuery()
	  		.deploymentId(id)
	  		.singleResult()
	  		.getId();
  }
	
	private String deployTestProcessWithTwoTasksNoTenant() {
	  String id = repositoryService.createDeployment()
			.addBpmnModel("testProcess.bpmn20.xml", createTwoTasksTestProcess())
			.deploy()
			.getId();
	  
	  autoCleanedUpDeploymentIds.add(id);
	  
	  return repositoryService.createProcessDefinitionQuery()
	  		.deploymentId(id)
	  		.singleResult()
	  		.getId();
  }
	
	
	public void testDeploymentTenancy() {
		
		deployTestProcessWithTestTenant();
		
		assertEquals(TEST_TENANT_ID, repositoryService.createDeploymentQuery().singleResult().getTenantId());
		assertEquals(1, repositoryService.createDeploymentQuery().deploymentTenantId(TEST_TENANT_ID).list().size());
		assertEquals(1, repositoryService.createDeploymentQuery().deploymentId(autoCleanedUpDeploymentIds.get(0)).deploymentTenantId(TEST_TENANT_ID).list().size());
		assertEquals(1, repositoryService.createDeploymentQuery().deploymentTenantIdLike("my%").list().size());
		assertEquals(1, repositoryService.createDeploymentQuery().deploymentTenantIdLike("%TenantId").list().size());
		assertEquals(1, repositoryService.createDeploymentQuery().deploymentTenantIdLike("m%Ten%").list().size());
		assertEquals(0, repositoryService.createDeploymentQuery().deploymentTenantIdLike("noexisting%").list().size());
		assertEquals(0, repositoryService.createDeploymentQuery().deploymentWithoutTenantId().list().size());
	}

	public void testProcessDefinitionTenancy() {

		// Deploy a process with tenant and verify
		
		String processDefinitionIdWithTenant = deployTestProcessWithTestTenant();
		assertEquals(TEST_TENANT_ID, repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionIdWithTenant).singleResult().getTenantId());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(TEST_TENANT_ID).list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("m%").list().size());
		assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("somethingElse%").list().size());
		assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionWithoutTenantId().list().size());
		
		// Deploy another process, without tenant
		String processDefinitionIdWithoutTenant = deployOneTaskTestProcess();
		assertEquals(2, repositoryService.createProcessDefinitionQuery().list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(TEST_TENANT_ID).list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("m%").list().size());
		assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("somethingElse%").list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionWithoutTenantId().list().size());
		
		// Deploy another process with the same tenant
		String processDefinitionIdWithTenant2 = deployTestProcessWithTestTenant();
		assertEquals(3, repositoryService.createProcessDefinitionQuery().list().size());
		assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(TEST_TENANT_ID).list().size());
		assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("m%").list().size());
		assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("somethingElse%").list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionWithoutTenantId().list().size());
		
		// Extra check: we deployed the one task process twice, but once with tenant and once without. The latest query should show this.
		assertEquals(processDefinitionIdWithTenant2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(TEST_TENANT_ID).latestVersion().singleResult().getId());
		assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId("Not a tenant").latestVersion().count());
		assertEquals(processDefinitionIdWithoutTenant, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionWithoutTenantId().latestVersion().singleResult().getId());
	}
	
	public void testProcessInstanceTenancy() {
		
		// Start a number of process instances with tenant
		String processDefinitionId = deployTestProcessWithTestTenant();
		int nrOfProcessInstancesWithTenant = 6;
		for (int i=0; i<nrOfProcessInstancesWithTenant; i++) {
			runtimeService.startProcessInstanceById(processDefinitionId);
		}
		
		// Start a number of process instance without tenantid
		String processDefinitionIdNoTenant = deployOneTaskTestProcess();
		int nrOfProcessInstancesNoTenant = 8;
		for (int i=0; i<nrOfProcessInstancesNoTenant; i++) {
			runtimeService.startProcessInstanceById(processDefinitionIdNoTenant);
		}
		
		// Check the query results
		assertEquals(TEST_TENANT_ID, runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinitionId).list().get(0).getTenantId());
		assertEquals(nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant, runtimeService.createProcessInstanceQuery().list().size());
		assertEquals(nrOfProcessInstancesNoTenant, runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().list().size());
		assertEquals(nrOfProcessInstancesWithTenant, runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).list().size());
		assertEquals(nrOfProcessInstancesWithTenant, runtimeService.createProcessInstanceQuery().processInstanceTenantIdLike("%enan%").list().size());
		
	}
	
	public void testExecutionTenancy() {
		
		// Start a number of process instances with tenant
		String processDefinitionId = deployTestProcessWithTwoTasksWithTestTenant();
		int nrOfProcessInstancesWithTenant = 4;
		for (int i=0; i<nrOfProcessInstancesWithTenant; i++) {
			runtimeService.startProcessInstanceById(processDefinitionId);
		}
		
		// Start a number of process instance without tenantid
		String processDefinitionIdNoTenant = deployTwoTasksTestProcess();
		int nrOfProcessInstancesNoTenant = 2;
		for (int i=0; i<nrOfProcessInstancesNoTenant; i++) {
			runtimeService.startProcessInstanceById(processDefinitionIdNoTenant);
		}
		
		// Check the query results:
		// note: 3 executions per process instance due to parallelism!
		assertEquals(TEST_TENANT_ID, runtimeService.createExecutionQuery().processDefinitionId(processDefinitionId).list().get(0).getTenantId());
		assertEquals("", runtimeService.createExecutionQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId());
		assertEquals(3 * (nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant), runtimeService.createExecutionQuery().list().size());
		assertEquals(3 * nrOfProcessInstancesNoTenant, runtimeService.createExecutionQuery().executionWithoutTenantId().list().size());
		assertEquals(3 * nrOfProcessInstancesWithTenant, runtimeService.createExecutionQuery().executionTenantId(TEST_TENANT_ID).list().size());
		assertEquals(3 * nrOfProcessInstancesWithTenant, runtimeService.createExecutionQuery().executionTenantIdLike("%en%").list().size());
		
		// Check the process instance query results, just to be sure
		assertEquals(TEST_TENANT_ID, runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinitionId).list().get(0).getTenantId());
		assertEquals(nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant, runtimeService.createProcessInstanceQuery().list().size());
		assertEquals(nrOfProcessInstancesNoTenant, runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().list().size());
		assertEquals(nrOfProcessInstancesWithTenant, runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).list().size());
		assertEquals(nrOfProcessInstancesWithTenant, runtimeService.createProcessInstanceQuery().processInstanceTenantIdLike("%en%").list().size());
	}
	
	public void testTaskTenancy() {
		
		// Generate 10 tasks with tenant
		String processDefinitionIdWithTenant = deployTestProcessWithTwoTasksWithTestTenant();
		int nrOfProcessInstancesWithTenant = 5;
		for (int i=0; i<nrOfProcessInstancesWithTenant; i++) {
			runtimeService.startProcessInstanceById(processDefinitionIdWithTenant);
		}
		
		// Generate 4 tasks without tenant
		String processDefinitionIdNoTenant = deployTwoTasksTestProcess();
		int nrOfProcessInstancesNoTenant = 2;
		for (int i = 0; i < nrOfProcessInstancesNoTenant; i++) {
			runtimeService.startProcessInstanceById(processDefinitionIdNoTenant);
		}
		
		// Check the query results
		assertEquals(TEST_TENANT_ID, taskService.createTaskQuery().processDefinitionId(processDefinitionIdWithTenant).list().get(0).getTenantId());
		assertEquals("", taskService.createTaskQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId());
		
		assertEquals(14, taskService.createTaskQuery().list().size());
		assertEquals(10, taskService.createTaskQuery().taskTenantId(TEST_TENANT_ID).list().size());
		assertEquals(0, taskService.createTaskQuery().taskTenantId("Another").list().size());
		assertEquals(10, taskService.createTaskQuery().taskTenantIdLike("my%").list().size());
		assertEquals(4, taskService.createTaskQuery().taskWithoutTenantId().list().size());
		
	}
	
	public void testJobTenancy() {
		
		// Deploy process with a timer and an async step AND with a tenant
		String deploymentId = repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testJobTenancy.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy()
			.getId();
		
		// verify job (timer start) 
		Job job = managementService.createJobQuery().singleResult();
		assertEquals(TEST_TENANT_ID, job.getTenantId());
		managementService.executeJob(job.getId());
		
		// Verify Job tenancy (process intermediary timer)
		job = managementService.createJobQuery().singleResult();
		assertEquals(TEST_TENANT_ID, job.getTenantId());
		
		// Start process, and verify async job has correct tenant id
		managementService.executeJob(job.getId());
		job = managementService.createJobQuery().singleResult();
		assertEquals(TEST_TENANT_ID, job.getTenantId());
		
		// Finish process
		managementService.executeJob(job.getId());
		
		// Do the same, but now without a tenant
		String deploymentId2 = repositoryService.createDeployment()
				.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testJobTenancy.bpmn20.xml")
				.deploy()
				.getId();
		
		job = managementService.createJobQuery().singleResult();
		assertEquals("", job.getTenantId());
		managementService.executeJob(job.getId());
		job = managementService.createJobQuery().singleResult();
		assertEquals("", job.getTenantId());
		managementService.executeJob(job.getId());
		job = managementService.createJobQuery().singleResult();
		assertEquals("", job.getTenantId());
		
		// clean up
		repositoryService.deleteDeployment(deploymentId, true);
		repositoryService.deleteDeployment(deploymentId2, true);
	}
	
	public void testModelTenancy() {

		// Create a few models with tenant
		int nrOfModelsWithTenant = 3;
		for (int i = 0; i < nrOfModelsWithTenant; i++) {
			Model model = repositoryService.newModel();
			model.setName(i + "");
			model.setTenantId(TEST_TENANT_ID);
			repositoryService.saveModel(model);
		}

		// Create a few models without tenant
		int nrOfModelsWithoutTenant = 5;
		for (int i = 0; i < nrOfModelsWithoutTenant; i++) {
			Model model = repositoryService.newModel();
			model.setName(i + "");
			repositoryService.saveModel(model);
		}
		
		// Check query
		assertEquals(nrOfModelsWithoutTenant + nrOfModelsWithTenant, repositoryService.createModelQuery().list().size());
		assertEquals(nrOfModelsWithoutTenant, repositoryService.createModelQuery().modelWithoutTenantId().list().size());
		assertEquals(nrOfModelsWithTenant, repositoryService.createModelQuery().modelTenantId(TEST_TENANT_ID).list().size());
		assertEquals(nrOfModelsWithTenant, repositoryService.createModelQuery().modelTenantIdLike("my%").list().size());
		assertEquals(0, repositoryService.createModelQuery().modelTenantId("a%").list().size());

		// Clean up
		for (Model model : repositoryService.createModelQuery().list()) {
			repositoryService.deleteModel(model.getId());
		}
		
	}
	
	public void testChangeDeploymentTenantId() {
		
		// Generate 8 tasks with tenant
		String processDefinitionIdWithTenant = deployTestProcessWithTwoTasksWithTestTenant();
		int nrOfProcessInstancesWithTenant = 4;
		for (int i=0; i<nrOfProcessInstancesWithTenant; i++) {
			runtimeService.startProcessInstanceById(processDefinitionIdWithTenant);
		}
		
		// Generate 10 tasks without tenant
		String processDefinitionIdNoTenant = deployTwoTasksTestProcess();
		int nrOfProcessInstancesNoTenant = 5;
		for (int i = 0; i < nrOfProcessInstancesNoTenant; i++) {
			runtimeService.startProcessInstanceById(processDefinitionIdNoTenant);
		}
		
		// Migrate deployment with tenant to another tenant
		String newTenantId = "NEW TENANT ID";
		
		String deploymentId = repositoryService.createProcessDefinitionQuery()
				.processDefinitionId(processDefinitionIdWithTenant).singleResult().getDeploymentId();
		repositoryService.changeDeploymentTenantId(deploymentId, newTenantId);
		
		// Verify tenant id
		Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
		assertEquals(newTenantId, deployment.getTenantId());
		
		// Verify deployment
		assertEquals(2, repositoryService.createDeploymentQuery().list().size());
		assertEquals(0, repositoryService.createDeploymentQuery().deploymentTenantId(TEST_TENANT_ID).list().size());
		assertEquals(1, repositoryService.createDeploymentQuery().deploymentTenantId(newTenantId).list().size());
		assertEquals(1, repositoryService.createDeploymentQuery().deploymentWithoutTenantId().list().size());
		
		// Verify process definition
		assertEquals(2, repositoryService.createProcessDefinitionQuery().list().size());
		assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(TEST_TENANT_ID).list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(newTenantId).list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(newTenantId).list().size());
		
		// Verify process instances
		assertEquals(nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant, runtimeService.createProcessInstanceQuery().list().size());
		assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).list().size());
		assertEquals(nrOfProcessInstancesWithTenant, runtimeService.createProcessInstanceQuery().processInstanceTenantId(newTenantId).list().size());
		assertEquals(nrOfProcessInstancesNoTenant, runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().list().size());
		
		// Verify executions
		assertEquals(3 * (nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant), runtimeService.createExecutionQuery().list().size());
		assertEquals(3 * nrOfProcessInstancesNoTenant, runtimeService.createExecutionQuery().executionWithoutTenantId().list().size());
		assertEquals(0, runtimeService.createExecutionQuery().executionTenantId(TEST_TENANT_ID).list().size());
		assertEquals(3 * nrOfProcessInstancesWithTenant, runtimeService.createExecutionQuery().executionTenantId(newTenantId).list().size());
		assertEquals(3 * nrOfProcessInstancesWithTenant, runtimeService.createExecutionQuery().executionTenantIdLike("NEW%").list().size());
		
		// Verify tasks
		assertEquals(2 * (nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant), taskService.createTaskQuery().list().size());
		assertEquals(0, taskService.createTaskQuery().taskTenantId(TEST_TENANT_ID).list().size());
		assertEquals(2 * nrOfProcessInstancesWithTenant, taskService.createTaskQuery().taskTenantId(newTenantId).list().size());
		assertEquals(2 * nrOfProcessInstancesNoTenant, taskService.createTaskQuery().taskWithoutTenantId().list().size());
		
		
		// Remove the tenant id and verify results
		try {
			repositoryService.changeDeploymentTenantId(deploymentId, "");
			fail(); // should clash: there is already a process definition with the same key
		} catch (Exception e) {
			
		}
	}
	
	public void testChangeDeploymentIdWithClash() {
		String processDefinitionIdWithTenant = deployTestProcessWithTestTenant("tenantA");
		String processDefinitionIdNoTenant = deployOneTaskTestProcess();
		
		// Changing the one with tenant now back to one without should clash, cause there already exists one
		try {
			repositoryService.changeDeploymentTenantId(processDefinitionIdWithTenant, "");
			fail();
		} catch (Exception e) {}
		
		// Deploying another version should just up the version
		String processDefinitionIdNoTenant2 = deployOneTaskTestProcess();
		assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionIdNoTenant2).singleResult().getVersion());
		
	}
	
	public void testJobTenancyAfterTenantChange() {
		
		// Deploy process with a timer and an async step AND with a tenant
		String deploymentId = repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testJobTenancy.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy()
			.getId();
		
		String newTenant = "newTenant";
		repositoryService.changeDeploymentTenantId(deploymentId, newTenant);
		
		// verify job (timer start) 
		Job job = managementService.createJobQuery().singleResult();
		assertEquals(newTenant, job.getTenantId());
		managementService.executeJob(job.getId());
		
		// Verify Job tenancy (process intermediary timer)
		job = managementService.createJobQuery().singleResult();
		assertEquals(newTenant, job.getTenantId());
		
		// Start process, and verify async job has correct tenant id
		managementService.executeJob(job.getId());
		job = managementService.createJobQuery().singleResult();
		assertEquals(newTenant, job.getTenantId());
		
		// Finish process
		managementService.executeJob(job.getId());
		
		// clean up
		repositoryService.deleteDeployment(deploymentId, true);
	}
	
	public void testHistoryTenancy() {
		
		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
		
			// Generate 3 tasks with tenant
			String processDefinitionIdWithTenant = deployTestProcessWithTestTenant();
			int nrOfProcessInstancesWithTenant = 3;
			for (int i=0; i<nrOfProcessInstancesWithTenant; i++) {
				runtimeService.startProcessInstanceById(processDefinitionIdWithTenant);
			}
			
			// Generate 2 tasks without tenant
			String processDefinitionIdNoTenant = deployOneTaskTestProcess();
			int nrOfProcessInstancesNoTenant = 2;
			for (int i = 0; i < nrOfProcessInstancesNoTenant; i++) {
				runtimeService.startProcessInstanceById(processDefinitionIdNoTenant);
			}
			
			// Complete all tasks
			for (Task task : taskService.createTaskQuery().list()) {
				taskService.complete(task.getId());
			}
			
			// Verify process instances
			assertEquals(TEST_TENANT_ID, historyService.createHistoricProcessInstanceQuery().processDefinitionId(processDefinitionIdWithTenant).list().get(0).getTenantId());
			assertEquals("", historyService.createHistoricProcessInstanceQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId());
			assertEquals(nrOfProcessInstancesWithTenant + nrOfProcessInstancesNoTenant, historyService.createHistoricProcessInstanceQuery().list().size());
			assertEquals(nrOfProcessInstancesWithTenant, historyService.createHistoricProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).list().size());
			assertEquals(nrOfProcessInstancesWithTenant, historyService.createHistoricProcessInstanceQuery().processInstanceTenantIdLike("%e%").list().size());
			assertEquals(nrOfProcessInstancesNoTenant, historyService.createHistoricProcessInstanceQuery().processInstanceWithoutTenantId().list().size());
			
			// verify tasks
			assertEquals(TEST_TENANT_ID, historyService.createHistoricTaskInstanceQuery().processDefinitionId(processDefinitionIdWithTenant).list().get(0).getTenantId());
			assertEquals("", historyService.createHistoricTaskInstanceQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId());
			assertEquals(nrOfProcessInstancesWithTenant + nrOfProcessInstancesNoTenant, historyService.createHistoricTaskInstanceQuery().list().size());
			assertEquals(nrOfProcessInstancesWithTenant, historyService.createHistoricTaskInstanceQuery().taskTenantId(TEST_TENANT_ID).list().size());
			assertEquals(nrOfProcessInstancesWithTenant, historyService.createHistoricTaskInstanceQuery().taskTenantIdLike("my%").list().size());
			assertEquals(nrOfProcessInstancesNoTenant, historyService.createHistoricTaskInstanceQuery().taskWithoutTenantId().list().size());
			
			// verify activities
			List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery().processDefinitionId(processDefinitionIdWithTenant).list();
			for (HistoricActivityInstance historicActivityInstance : activityInstances) {
				assertEquals(TEST_TENANT_ID, historicActivityInstance.getTenantId());
			}
			assertEquals("", historyService.createHistoricActivityInstanceQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId());
			assertEquals(3 * (nrOfProcessInstancesWithTenant + nrOfProcessInstancesNoTenant), historyService.createHistoricActivityInstanceQuery().list().size());
			assertEquals(3 * nrOfProcessInstancesWithTenant, historyService.createHistoricActivityInstanceQuery().activityTenantId(TEST_TENANT_ID).list().size());
			assertEquals(3 * nrOfProcessInstancesWithTenant, historyService.createHistoricActivityInstanceQuery().activityTenantIdLike("my%").list().size());
			assertEquals(3 * nrOfProcessInstancesNoTenant, historyService.createHistoricActivityInstanceQuery().activityWithoutTenantId().list().size());
			
		}
		
	}
	
	public void testProcessDefinitionKeyClashBetweenTenants() {
		
		String tentanA = "tenantA";
		String tenantB = "tenantB";
		
		// Deploy the same process (same process definition key) for two different tenants.
		String procDefIdA = deployTestProcessWithTestTenant(tentanA);
		String procDefIdB = deployTestProcessWithTestTenant(tenantB);
		
		// verify query
		assertEquals("oneTaskProcess", repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdA).singleResult().getKey());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdA).singleResult().getVersion());
		assertEquals("oneTaskProcess", repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdB).singleResult().getKey());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdB).singleResult().getVersion());
		assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tentanA).list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tenantB).list().size());
		assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionWithoutTenantId().list().size());
		
		// Deploy second version
		procDefIdA = deployTestProcessWithTestTenant(tentanA);
		
		assertEquals("oneTaskProcess", repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdA).singleResult().getKey());
		assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdA).singleResult().getVersion());
		assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tentanA).latestVersion().singleResult().getVersion());
		assertEquals("oneTaskProcess", repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdB).singleResult().getKey());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdB).singleResult().getVersion());
		assertEquals(3, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").list().size());
		assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tentanA).list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tenantB).list().size());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tentanA).latestVersion().list().size());
		assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionWithoutTenantId().list().size());
		
		// Now, start process instances by process definition key (no tenant)
		try {
			runtimeService.startProcessInstanceByKey("oneTaskProcess");
			fail(); // shouldnt happen, there is no process definition with that key that has no tenant, it has to give an exception as such!
		} catch(Exception e) {
		}
		
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", tentanA);
		assertEquals(procDefIdA, processInstance.getProcessDefinitionId());
		
		 processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", tenantB);
		 assertEquals(procDefIdB, processInstance.getProcessDefinitionId());
	}
	
	public void testSuspendProcessDefinitionTenancy() {
		
		// Deploy one process definition for tenant A, and two process definitions versions for tenant B
		String tentanA = "tenantA";
		String tenantB = "tenantB";
		
		String procDefIdA = deployTestProcessWithTestTenant(tentanA);
		String procDefIdB = deployTestProcessWithTestTenant(tenantB);
		String procDefIdB2 = deployTestProcessWithTestTenant(tenantB);
		
		// Suspend process definition B
		repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", tenantB);
		
		// Shouldn't be able to start proc defs for tentant B
		try {
			runtimeService.startProcessInstanceById(procDefIdB);
		} catch (ActivitiException e) {}
		
		try {
			runtimeService.startProcessInstanceById(procDefIdB2);
		} catch (ActivitiException e) {}
		
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(procDefIdA);
		Assert.assertNotNull(processInstance);
		
		// Activate process again
		repositoryService.activateProcessDefinitionByKey("oneTaskProcess", tenantB);
		
		processInstance = runtimeService.startProcessInstanceById(procDefIdB);
		Assert.assertNotNull(processInstance);
		
		processInstance = runtimeService.startProcessInstanceById(procDefIdB2);
		Assert.assertNotNull(processInstance);
		
		processInstance = runtimeService.startProcessInstanceById(procDefIdA);
		Assert.assertNotNull(processInstance);
		
		// Suspending with NO tenant id should give an error, cause they both have tenants
		try {
			repositoryService.suspendProcessDefinitionByKey("oneTaskProcess");
		} catch (ActivitiException e) {}
	}
	
	public void testSignalFromProcessTenancy() {
		
		// Deploy process both with and without tenant
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml")
			.deploy();
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy();
		
		// Start 3 proc instances for the one with a tenant and 2 for the one without tenant
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		
		// verify
		assertEquals(3, taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(2, taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count());
		
		// Now, start 1 process instance that fires a signal event (not in tenant context), it should obly continue those without tenant
		runtimeService.startProcessInstanceByKey("testMtSignalFiring");
		assertEquals(0, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(2, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		// Start a process instance that is running in tenant context
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalFiring", TEST_TENANT_ID);
		assertEquals(3, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(2, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		// Cleanup
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
		
	}
	
	public void testSignalThroughApiTenancy() {
		
		// Deploy process both with and without tenant
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml")
			.deploy();
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy();
		
		// Start 4 proc instances for the one with a tenant and 5 for the one without tenant
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		
		// Verify
		assertEquals(4, taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(5, taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count());
		
		// Signal through API (with tenant)
		runtimeService.signalEventReceivedWithTenantId("The Signal", TEST_TENANT_ID);
		assertEquals(4, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(0, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		// Signal through API (without tenant)
		runtimeService.signalEventReceived("The Signal");
		assertEquals(4, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(5, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		// Cleanup
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}
	
	public void testSignalThroughApiTenancyReversed() { // cause reversing the order of calling DID leave to an error!
		
		// Deploy process both with and without tenant
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml")
			.deploy();
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy();
		
		// Start 4 proc instances for the one with a tenant and 5 for the one without tenant
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		
		// Verify
		assertEquals(4, taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(5, taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count());
		
		// Signal through API (without tenant)
		runtimeService.signalEventReceived("The Signal");
		assertEquals(0, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(5, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		// Signal through API (with tenant)
		runtimeService.signalEventReceivedWithTenantId("The Signal", TEST_TENANT_ID);
		assertEquals(4, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(5, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		
		// Cleanup
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}
	
	public void testSignalAsyncThroughApiTenancy() {
		
		// Deploy process both with and without tenant
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml")
			.deploy();
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy();
		
		// Start 4 proc instances for the one with a tenant and 5 for the one without tenant
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		runtimeService.startProcessInstanceByKey("testMtSignalCatch");
		
		// Verify
		assertEquals(4, taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(5, taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count());
		
		// Signal through API (with tenant)
		runtimeService.signalEventReceivedAsyncWithTenantId("The Signal", TEST_TENANT_ID);
		assertEquals(0, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(0, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		for (Job job : managementService.createJobQuery().list()) {
			managementService.executeJob(job.getId());
		}
		
		assertEquals(4, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(0, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		// Signal through API (without tenant)
		runtimeService.signalEventReceivedAsync("The Signal");
		
		assertEquals(4, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(0, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		for (Job job : managementService.createJobQuery().list()) {
			managementService.executeJob(job.getId());
		}
		
		assertEquals(4, taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count());
		assertEquals(5, taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count());
		
		// Cleanup
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}
	
	public void testStartProcessInstanceBySignalTenancy() {
		
		// Deploy process both with and without tenant
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testStartProcessInstanceBySignalTenancy.bpmn20.xml")
			.deploy();
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testStartProcessInstanceBySignalTenancy.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy();
		
		// Signaling without tenant
		runtimeService.signalEventReceived("The Signal");
		assertEquals(3, runtimeService.createProcessInstanceQuery().count());
		assertEquals(3, runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().count());
		assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).count());
		
		// Signalling with tenant
		runtimeService.signalEventReceivedWithTenantId("The Signal", TEST_TENANT_ID);
		assertEquals(6, runtimeService.createProcessInstanceQuery().count());
		assertEquals(3, runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().count());
		assertEquals(3, runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).count());
		
		// Start a process instance with a boundary catch (with and without tenant)
		runtimeService.startProcessInstanceByKey("processWithSignalCatch");
		runtimeService.startProcessInstanceByKeyAndTenantId("processWithSignalCatch", TEST_TENANT_ID);
		
		runtimeService.signalEventReceived("The Signal");
		assertEquals(11, runtimeService.createProcessInstanceQuery().count());
		assertEquals(7, runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().count());
		assertEquals(4, runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).count());
		
		runtimeService.signalEventReceivedWithTenantId("The Signal", TEST_TENANT_ID);
		assertEquals(14, runtimeService.createProcessInstanceQuery().count());
		assertEquals(7, runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().count());
		assertEquals(7, runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).count());
		
		
		// Cleanup
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}
	
	public void testStartProcessInstanceByMessageTenancy() {
		
		// Deploy process both with and without tenant
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMessageTenancy.bpmn20.xml")
			.deploy();
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMessageTenancy.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy();
		
		// Verify query
		assertEquals(2, repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("My message").count());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("My message").processDefinitionWithoutTenantId().count());
		assertEquals(1, repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("My message").processDefinitionTenantId(TEST_TENANT_ID).count());
		
		// Start a process instance by message without tenant
		runtimeService.startProcessInstanceByMessage("My message");
		runtimeService.startProcessInstanceByMessage("My message");
		
		assertEquals(2, taskService.createTaskQuery().taskName("My task").count());
		assertEquals(2, taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count());
		assertEquals(0, taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count());
		
		// Start a process instance by message with tenant
		runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
		assertEquals(5, taskService.createTaskQuery().taskName("My task").count());
		assertEquals(2, taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count());
		assertEquals(3, taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count());
		
		// Cleanup
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}
	
	public void testStartProcessInstanceByMessageTenancyReversed() { // same as above, but now reversed
		
		// Deploy process both with and without tenant
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMessageTenancy.bpmn20.xml")
			.deploy();
		repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMessageTenancy.bpmn20.xml")
			.tenantId(TEST_TENANT_ID)
			.deploy();
		
		// Start a process instance by message with tenant
		runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
		runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
		assertEquals(3, taskService.createTaskQuery().taskName("My task").count());
		assertEquals(0, taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count());
		assertEquals(3, taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count());
		
		// Start a process instance by message without tenant
		runtimeService.startProcessInstanceByMessage("My message");
		runtimeService.startProcessInstanceByMessage("My message");
		
		assertEquals(5, taskService.createTaskQuery().taskName("My task").count());
		assertEquals(2, taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count());
		assertEquals(3, taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count());
		
		// Cleanup
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}
	
	// Bug from http://forums.activiti.org/content/callactiviti-tenant-id
	public void testCallActivityWithTenant() {
	  if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
  		String tenantId = "apache";
  		  
  		//deploying both processes. Process 1 will call Process 2 
  		repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testCallActivityWithTenant-process01.bpmn20.xml").tenantId(tenantId).deploy();
  		repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testCallActivityWithTenant-process02.bpmn20.xml").tenantId(tenantId).deploy();
  		  
  		//Starting Process 1. Process 1 will be executed successfully but when the call to process 2 is made internally it will throw the exception
  		ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("process1",null, CollectionUtil.singletonMap("sendFor", "test"), tenantId);
  		Assert.assertNotNull(processInstance);
  		  
  		Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("process2").processInstanceTenantId(tenantId).count());
  		Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("process2").count());
  		  
  		// following line if executed will give activiti object not found exception as the process1 is linked to a tenant id.
  		try {
  			processInstance = runtimeService.startProcessInstanceByKey("process1");
  			Assert.fail();
  		} catch (Exception e) {
  		  	
  		}
  		
  		// Cleanup
  		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
  			repositoryService.deleteDeployment(deployment.getId(), true);
  		}
	  }
	}
	
	/*
	 * See https://activiti.atlassian.net/browse/ACT-4034
	 */
	public void testGetLatestProcessDefinitionVersionForSameProcessDefinitionKey() {
		String tenant1 = "tenant1";
		String tenant2 = "tenant2";
		
		// Tenant 1 ==> version 4
		for (int i=0; i<4; i++) {
			deployTestProcessWithTestTenant(tenant1);
		}
		
		// Tenant 2 ==> version 2
		for (int i=0; i<2; i++) {
			deployTestProcessWithTestTenant(tenant2);
		}
		
		// No tenant ==> version 3
		for (int i=0; i<3; i++) {
			deployTestProcessWithTwoTasksNoTenant();
		}
		
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
			.processDefinitionTenantId(tenant1)
			.latestVersion()
			.singleResult();
		assertEquals(4, processDefinition.getVersion());
		
		processDefinition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionTenantId(tenant2)
				.latestVersion()
				.singleResult();
			assertEquals(2, processDefinition.getVersion());
			
			processDefinition = repositoryService.createProcessDefinitionQuery()
					.processDefinitionWithoutTenantId()
					.latestVersion()
					.singleResult();
			assertEquals(3, processDefinition.getVersion());
			
			List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().latestVersion().list();
			assertEquals(3, processDefinitions.size());
			
			// Verify they have different tenant ids
			int tenant1Count = 0, tenant2Count = 0, noTenantCount = 0 ;
			for (ProcessDefinition p : processDefinitions) {
				if (p.getTenantId() == null || p.getTenantId().equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
					noTenantCount++;
				} else if (p.getTenantId().equals(tenant1)) {
					tenant1Count++;
				} else if (p.getTenantId().equals(tenant2)) {
					tenant2Count++;
				}
			}
			assertEquals(1, tenant1Count);
			assertEquals(1, tenant2Count);
			assertEquals(1, noTenantCount);
	}
	
}
