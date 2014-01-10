package org.activiti.engine.test.api.tenant;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;

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
	  this.autoCleanedUpDeploymentIds.clear();;
	}
	
	@Override
	protected void tearDown() throws Exception {
	  super.tearDown();
	  
	  if (autoCleanedUpDeploymentIds.size() > 0) {
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
	  String id = repositoryService.createDeployment()
			.addBpmnModel("testProcess.bpmn20.xml", createOneTaskTestProcess())
			.tenantId(TEST_TENANT_ID)
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
		assertEquals(null, runtimeService.createExecutionQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId());
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
		assertEquals(null, taskService.createTaskQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId());
		
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
		
		// Start process instance 
		runtimeService.startProcessInstanceByKey("testJobTenancy");
		
		// Verify Job tenancy (process start timer)
		Job job = managementService.createJobQuery().singleResult();
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
			runtimeService.startProcessInstanceByKey("testJobTenancy");
			job = managementService.createJobQuery().singleResult();
			assertEquals(null, job.getTenantId());
			managementService.executeJob(job.getId());
			job = managementService.createJobQuery().singleResult();
			assertEquals(null, job.getTenantId());
		
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

}
