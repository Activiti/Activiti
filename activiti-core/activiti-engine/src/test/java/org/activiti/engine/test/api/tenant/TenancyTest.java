package org.activiti.engine.test.api.tenant;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * A test case for the various implications of the tenancy support (tenant id column to entities + query support)
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
                repositoryService.deleteDeployment(deploymentId,
                                                   true);
            }
        }
    }

    /**
     * Deploys the one task process with the test tenant id.
     * @return The process definition id of the deployed process definition.
     */
    private String deployTestProcessWithTestTenant() {
        return deployTestProcessWithTestTenant(TEST_TENANT_ID);
    }

    private String deployTestProcessWithTestTenant(String tenantId) {
        String id = repositoryService.createDeployment().addBpmnModel("testProcess.bpmn20.xml",
                                                                      createOneTaskTestProcess()).tenantId(tenantId).deploy().getId();

        autoCleanedUpDeploymentIds.add(id);

        return repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult().getId();
    }

    private String deployTestProcessWithTwoTasksWithTestTenant() {
        String id = repositoryService.createDeployment().addBpmnModel("testProcess.bpmn20.xml",
                                                                      createTwoTasksTestProcess()).tenantId(TEST_TENANT_ID).deploy().getId();

        autoCleanedUpDeploymentIds.add(id);

        return repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult().getId();
    }

    private String deployTestProcessWithTwoTasksNoTenant() {
        String id = repositoryService.createDeployment().addBpmnModel("testProcess.bpmn20.xml",
                                                                      createTwoTasksTestProcess()).deploy().getId();

        autoCleanedUpDeploymentIds.add(id);

        return repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult().getId();
    }

    public void testDeploymentTenancy() {

        deployTestProcessWithTestTenant();

        assertThat(repositoryService.createDeploymentQuery().singleResult().getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(TEST_TENANT_ID).list()).hasSize(1);
        assertThat(repositoryService.createDeploymentQuery().deploymentId(autoCleanedUpDeploymentIds.get(0)).deploymentTenantId(TEST_TENANT_ID).list()).hasSize(1);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantIdLike("my%").list()).hasSize(1);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantIdLike("%TenantId").list()).hasSize(1);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantIdLike("m%Ten%").list()).hasSize(1);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantIdLike("noexisting%").list()).hasSize(0);
        assertThat(repositoryService.createDeploymentQuery().deploymentWithoutTenantId().list()).hasSize(0);
    }

    public void testProcessDefinitionTenancy() {

        // Deploy a process with tenant and verify

        String processDefinitionIdWithTenant = deployTestProcessWithTestTenant();
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionIdWithTenant).singleResult().getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(TEST_TENANT_ID).list()).hasSize(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("m%").list()).hasSize(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("somethingElse%").list()).hasSize(0);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionWithoutTenantId().list()).hasSize(0);

        // Deploy another process, without tenant
        String processDefinitionIdWithoutTenant = deployOneTaskTestProcess();
        assertThat(repositoryService.createProcessDefinitionQuery().list()).hasSize(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(TEST_TENANT_ID).list()).hasSize(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("m%").list()).hasSize(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("somethingElse%").list()).hasSize(0);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionWithoutTenantId().list()).hasSize(1);

        // Deploy another process with the same tenant
        String processDefinitionIdWithTenant2 = deployTestProcessWithTestTenant();
        assertThat(repositoryService.createProcessDefinitionQuery().list()).hasSize(3);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(TEST_TENANT_ID).list()).hasSize(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("m%").list()).hasSize(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("somethingElse%").list()).hasSize(0);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionWithoutTenantId().list()).hasSize(1);

        // Extra check: we deployed the one task process twice, but once with
        // tenant and once without. The latest query should show this.
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(TEST_TENANT_ID).latestVersion()
                             .singleResult().getId()).isEqualTo(processDefinitionIdWithTenant2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId("Not a tenant").latestVersion().count()).isEqualTo(0);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionWithoutTenantId().latestVersion().singleResult().getId()).isEqualTo(processDefinitionIdWithoutTenant);
    }

    public void testProcessInstanceTenancy() {

        // Start a number of process instances with tenant
        String processDefinitionId = deployTestProcessWithTestTenant();
        int nrOfProcessInstancesWithTenant = 6;
        for (int i = 0; i < nrOfProcessInstancesWithTenant; i++) {
            runtimeService.startProcessInstanceById(processDefinitionId);
        }

        // Start a number of process instance without tenantit
        String processDefinitionIdNoTenant = deployOneTaskTestProcess();
        int nrOfProcessInstancesNoTenant = 8;
        for (int i = 0; i < nrOfProcessInstancesNoTenant; i++) {
            runtimeService.startProcessInstanceById(processDefinitionIdNoTenant);
        }

        // Check the query results
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinitionId).list().get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(runtimeService.createProcessInstanceQuery().list().size()).isEqualTo(nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().list().size()).isEqualTo(nrOfProcessInstancesNoTenant);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).list().size()).isEqualTo(nrOfProcessInstancesWithTenant);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantIdLike("%enan%").list().size()).isEqualTo(nrOfProcessInstancesWithTenant);
    }

    public void testExecutionTenancy() {

        // Start a number of process instances with tenant
        String processDefinitionId = deployTestProcessWithTwoTasksWithTestTenant();
        int nrOfProcessInstancesWithTenant = 4;
        for (int i = 0; i < nrOfProcessInstancesWithTenant; i++) {
            runtimeService.startProcessInstanceById(processDefinitionId);
        }

        // Start a number of process instance without tenantid
        String processDefinitionIdNoTenant = deployTwoTasksTestProcess();
        int nrOfProcessInstancesNoTenant = 2;
        for (int i = 0; i < nrOfProcessInstancesNoTenant; i++) {
            runtimeService.startProcessInstanceById(processDefinitionIdNoTenant);
        }

        // Check the query results:
        // note: 3 executions per process instance due to parallelism!
        assertThat(runtimeService.createExecutionQuery().processDefinitionId(processDefinitionId).list().get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(runtimeService.createExecutionQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId()).isEqualTo("");
        assertThat(runtimeService.createExecutionQuery().list().size()).isEqualTo(3 * (nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant));
        assertThat(runtimeService.createExecutionQuery().executionWithoutTenantId().list().size()).isEqualTo(3 * nrOfProcessInstancesNoTenant);
        assertThat(runtimeService.createExecutionQuery().executionTenantId(TEST_TENANT_ID).list().size()).isEqualTo(3 * nrOfProcessInstancesWithTenant);
        assertThat(runtimeService.createExecutionQuery().executionTenantIdLike("%en%").list().size()).isEqualTo(3 * nrOfProcessInstancesWithTenant);

        // Check the process instance query results, just to be sure
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinitionId).list().get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(runtimeService.createProcessInstanceQuery().list().size()).isEqualTo(nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().list().size()).isEqualTo(nrOfProcessInstancesNoTenant);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).list().size()).isEqualTo(nrOfProcessInstancesWithTenant);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantIdLike("%en%").list().size()).isEqualTo(nrOfProcessInstancesWithTenant);
    }

    public void testTaskTenancy() {

        // Generate 10 tasks with tenant
        String processDefinitionIdWithTenant = deployTestProcessWithTwoTasksWithTestTenant();
        int nrOfProcessInstancesWithTenant = 5;
        for (int i = 0; i < nrOfProcessInstancesWithTenant; i++) {
            runtimeService.startProcessInstanceById(processDefinitionIdWithTenant);
        }

        // Generate 4 tasks without tenant
        String processDefinitionIdNoTenant = deployTwoTasksTestProcess();
        int nrOfProcessInstancesNoTenant = 2;
        for (int i = 0; i < nrOfProcessInstancesNoTenant; i++) {
            runtimeService.startProcessInstanceById(processDefinitionIdNoTenant);
        }

        // Check the query results
        assertThat(taskService.createTaskQuery().processDefinitionId(processDefinitionIdWithTenant).list().get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(taskService.createTaskQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId()).isEqualTo("");

        assertThat(taskService.createTaskQuery().list().size()).isEqualTo(14);
        assertThat(taskService.createTaskQuery().taskTenantId(TEST_TENANT_ID).list().size()).isEqualTo(10);
        assertThat(taskService.createTaskQuery().taskTenantId("Another").list().size()).isEqualTo(0);
        assertThat(taskService.createTaskQuery().taskTenantIdLike("my%").list().size()).isEqualTo(10);
        assertThat(taskService.createTaskQuery().taskWithoutTenantId().list().size()).isEqualTo(4);
    }

    public void testJobTenancy() {

        // Deploy process with a timer and an async step AND with a tenant
        String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testJobTenancy.bpmn20.xml").tenantId(TEST_TENANT_ID).deploy()
                .getId();

        // verify job (timer start)
        Job job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo(TEST_TENANT_ID);
        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());

        // Verify Job tenancy (process intermediary timer)
        job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo(TEST_TENANT_ID);

        // Start process, and verify async job has correct tenant id
        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());
        job = managementService.createJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo(TEST_TENANT_ID);

        // Finish process
        managementService.executeJob(job.getId());

        // Do the same, but now without a tenant
        String deploymentId2 = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testJobTenancy.bpmn20.xml").deploy().getId();

        job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo("");
        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());
        job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo("");
        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());
        job = managementService.createJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo("");

        // clean up
        repositoryService.deleteDeployment(deploymentId,
                                           true);
        repositoryService.deleteDeployment(deploymentId2,
                                           true);
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
        assertThat(repositoryService.createModelQuery().list().size()).isEqualTo(nrOfModelsWithoutTenant + nrOfModelsWithTenant);
        assertThat(repositoryService.createModelQuery().modelWithoutTenantId().list().size()).isEqualTo(nrOfModelsWithoutTenant);
        assertThat(repositoryService.createModelQuery().modelTenantId(TEST_TENANT_ID).list().size()).isEqualTo(nrOfModelsWithTenant);
        assertThat(repositoryService.createModelQuery().modelTenantIdLike("my%").list().size()).isEqualTo(nrOfModelsWithTenant);
        assertThat(repositoryService.createModelQuery().modelTenantId("a%").list().size()).isEqualTo(0);

        // Clean up
        for (Model model : repositoryService.createModelQuery().list()) {
            repositoryService.deleteModel(model.getId());
        }
    }

    public void testChangeDeploymentTenantId() {

        // Generate 8 tasks with tenant
        String processDefinitionIdWithTenant = deployTestProcessWithTwoTasksWithTestTenant();
        int nrOfProcessInstancesWithTenant = 4;
        for (int i = 0; i < nrOfProcessInstancesWithTenant; i++) {
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

        String deploymentId = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionIdWithTenant).singleResult().getDeploymentId();
        repositoryService.changeDeploymentTenantId(deploymentId, newTenantId);

        // Verify tenant id
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
        assertThat(deployment.getTenantId()).isEqualTo(newTenantId);

        // Verify deployment
        assertThat(repositoryService.createDeploymentQuery().list()).hasSize(2);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(TEST_TENANT_ID).list()).hasSize(0);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(newTenantId).list()).hasSize(1);
        assertThat(repositoryService.createDeploymentQuery().deploymentWithoutTenantId().list()).hasSize(1);

        // Verify process definition
        assertThat(repositoryService.createProcessDefinitionQuery().list()).hasSize(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(TEST_TENANT_ID).list()).hasSize(0);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(newTenantId).list()).hasSize(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(newTenantId).list()).hasSize(1);

        // Verify process instances
        assertThat(runtimeService.createProcessInstanceQuery().list()).hasSize(nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).list()).hasSize(0);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantId(newTenantId).list()).hasSize(nrOfProcessInstancesWithTenant);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().list()).hasSize(nrOfProcessInstancesNoTenant);

        // Verify executions
        assertThat(runtimeService.createExecutionQuery().list()).hasSize(3 * (nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant));
        assertThat(runtimeService.createExecutionQuery().executionWithoutTenantId().list()).hasSize(3 * nrOfProcessInstancesNoTenant);
        assertThat(runtimeService.createExecutionQuery().executionTenantId(TEST_TENANT_ID).list()).hasSize(0);
        assertThat(runtimeService.createExecutionQuery().executionTenantId(newTenantId).list()).hasSize(3 * nrOfProcessInstancesWithTenant);
        assertThat(runtimeService.createExecutionQuery().executionTenantIdLike("NEW%").list()).hasSize(3 * nrOfProcessInstancesWithTenant);

        // Verify tasks
        assertThat(taskService.createTaskQuery().list()).hasSize(2 * (nrOfProcessInstancesNoTenant + nrOfProcessInstancesWithTenant));
        assertThat(taskService.createTaskQuery().taskTenantId(TEST_TENANT_ID).list()).hasSize(0);
        assertThat(taskService.createTaskQuery().taskTenantId(newTenantId).list()).hasSize(2 * nrOfProcessInstancesWithTenant);
        assertThat(taskService.createTaskQuery().taskWithoutTenantId().list()).hasSize(2 * nrOfProcessInstancesNoTenant);

        // Remove the tenant id and verify results
        // should clash: there is already a process definition with the same key
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> repositoryService.changeDeploymentTenantId(deploymentId, ""));
    }

    public void testChangeDeploymentIdWithClash() {
        String processDefinitionIdWithTenant = deployTestProcessWithTestTenant("tenantA");
        deployOneTaskTestProcess();

        // Changing the one with tenant now back to one without should clash,
        // cause there already exists one
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> repositoryService.changeDeploymentTenantId(processDefinitionIdWithTenant, ""));

        // Deploying another version should just up the version
        String processDefinitionIdNoTenant2 = deployOneTaskTestProcess();
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionIdNoTenant2).singleResult().getVersion()).isEqualTo(2);
    }

    public void testJobTenancyAfterTenantChange() {

        // Deploy process with a timer and an async step AND with a tenant
        String deploymentId = repositoryService.createDeployment()
            .addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testJobTenancy.bpmn20.xml").tenantId(TEST_TENANT_ID)
            .deploy()
            .getId();

        String newTenant = "newTenant";
        repositoryService.changeDeploymentTenantId(deploymentId, newTenant);

        // verify job (timer start)
        Job job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo(newTenant);
        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());

        // Verify Job tenancy (process intermediary timer)
        job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo(newTenant);

        // Start process, and verify async job has correct tenant id
        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());
        job = managementService.createJobQuery().singleResult();
        assertThat(job.getTenantId()).isEqualTo(newTenant);

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
            for (int i = 0; i < nrOfProcessInstancesWithTenant; i++) {
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
            assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionId(processDefinitionIdWithTenant).list().get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
            assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId()).isEqualTo("");
            assertThat(historyService.createHistoricProcessInstanceQuery().list()).hasSize(nrOfProcessInstancesWithTenant + nrOfProcessInstancesNoTenant);
            assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).list()).hasSize(nrOfProcessInstancesWithTenant);
            assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceTenantIdLike("%e%").list()).hasSize(nrOfProcessInstancesWithTenant);
            assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceWithoutTenantId().list()).hasSize(nrOfProcessInstancesNoTenant);

            // verify tasks
            assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionId(processDefinitionIdWithTenant).list().get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
            assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId()).isEqualTo("");
            assertThat(historyService.createHistoricTaskInstanceQuery().list()).hasSize(nrOfProcessInstancesWithTenant + nrOfProcessInstancesNoTenant);
            assertThat(historyService.createHistoricTaskInstanceQuery().taskTenantId(TEST_TENANT_ID).list()).hasSize(nrOfProcessInstancesWithTenant);
            assertThat(historyService.createHistoricTaskInstanceQuery().taskTenantIdLike("my%").list()).hasSize(nrOfProcessInstancesWithTenant);
            assertThat(historyService.createHistoricTaskInstanceQuery().taskWithoutTenantId().list()).hasSize(nrOfProcessInstancesNoTenant);

            // verify activities
            List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery().processDefinitionId(processDefinitionIdWithTenant).list();
            for (HistoricActivityInstance historicActivityInstance : activityInstances) {
                assertThat(historicActivityInstance.getTenantId()).isEqualTo(TEST_TENANT_ID);
            }
            assertThat(historyService.createHistoricActivityInstanceQuery().processDefinitionId(processDefinitionIdNoTenant).list().get(0).getTenantId()).isEqualTo("");
            assertThat(historyService.createHistoricActivityInstanceQuery().list()).hasSize(3 * (nrOfProcessInstancesWithTenant + nrOfProcessInstancesNoTenant));
            assertThat(historyService.createHistoricActivityInstanceQuery().activityTenantId(TEST_TENANT_ID).list()).hasSize(3 * nrOfProcessInstancesWithTenant);
            assertThat(historyService.createHistoricActivityInstanceQuery().activityTenantIdLike("my%").list()).hasSize(3 * nrOfProcessInstancesWithTenant);
            assertThat(historyService.createHistoricActivityInstanceQuery().activityWithoutTenantId().list()).hasSize(3 * nrOfProcessInstancesNoTenant);
        }
    }

    public void testProcessDefinitionKeyClashBetweenTenants() {

        String tentanA = "tenantA";
        String tenantB = "tenantB";

        // Deploy the same process (same process definition key) for two different tenants.
        String procDefIdA = deployTestProcessWithTestTenant(tentanA);
        String procDefIdB = deployTestProcessWithTestTenant(tenantB);

        // verify query
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdA).singleResult().getKey()).isEqualTo("oneTaskProcess");
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdA).singleResult().getVersion()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdB).singleResult().getKey()).isEqualTo("oneTaskProcess");
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdB).singleResult().getVersion()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").list()).hasSize(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tentanA).list()).hasSize(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tenantB).list()).hasSize(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionWithoutTenantId().list()).hasSize(0);

        // Deploy second version
        procDefIdA = deployTestProcessWithTestTenant(tentanA);

        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdA).singleResult().getKey()).isEqualTo("oneTaskProcess");
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdA).singleResult().getVersion()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tentanA).latestVersion().singleResult().getVersion()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdB).singleResult().getKey()).isEqualTo("oneTaskProcess");
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefIdB).singleResult().getVersion()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").list().size()).isEqualTo(3);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tentanA).list().size()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tenantB).list().size()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionTenantId(tentanA).latestVersion().list().size()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").processDefinitionWithoutTenantId().list().size()).isEqualTo(0);

        // Now, start process instances by process definition key (no tenant)
        // shouldn't happen, there is no process definition with that
        // key that has no tenant, it has to give an exception as such!
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> runtimeService.startProcessInstanceByKey("oneTaskProcess"));

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess",
                                                                                              tentanA);
        assertThat(processInstance.getProcessDefinitionId()).isEqualTo(procDefIdA);

        processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess",
                                                                              tenantB);
        assertThat(processInstance.getProcessDefinitionId()).isEqualTo(procDefIdB);
    }

    public void testSuspendProcessDefinitionTenancy() {

        // Deploy one process definition for tenant A, and two process
        // definitions versions for tenant B
        String tentanA = "tenantA";
        String tenantB = "tenantB";

        String procDefIdA = deployTestProcessWithTestTenant(tentanA);
        String procDefIdB = deployTestProcessWithTestTenant(tenantB);
        String procDefIdB2 = deployTestProcessWithTestTenant(tenantB);

        // Suspend process definition B
        repositoryService.suspendProcessDefinitionByKey("oneTaskProcess",
                                                        tenantB);

        // Shouldn't be able to start proc defs for tentant B
        try {
            runtimeService.startProcessInstanceById(procDefIdB);
        } catch (ActivitiException e) {
        }

        try {
            runtimeService.startProcessInstanceById(procDefIdB2);
        } catch (ActivitiException e) {
        }

        ProcessInstance processInstance = runtimeService.startProcessInstanceById(procDefIdA);
        assertThat(processInstance).isNotNull();

        // Activate process again
        repositoryService.activateProcessDefinitionByKey("oneTaskProcess",
                                                         tenantB);

        processInstance = runtimeService.startProcessInstanceById(procDefIdB);
        assertThat(processInstance).isNotNull();

        processInstance = runtimeService.startProcessInstanceById(procDefIdB2);
        assertThat(processInstance).isNotNull();

        processInstance = runtimeService.startProcessInstanceById(procDefIdA);
        assertThat(processInstance).isNotNull();

        // Suspending with NO tenant id should give an error, cause they both
        // have tenants
        try {
            repositoryService.suspendProcessDefinitionByKey("oneTaskProcess");
        } catch (ActivitiException e) {
        }
    }

    public void testSignalFromProcessTenancy() {

        // Deploy process both with and without tenant
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml").deploy();
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml").tenantId(TEST_TENANT_ID).deploy();

        // Start 3 proc instances for the one with a tenant and 2 for the one
        // without tenant
        runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch",
                                                            TEST_TENANT_ID);
        runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch",
                                                            TEST_TENANT_ID);
        runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalCatch",
                                                            TEST_TENANT_ID);
        runtimeService.startProcessInstanceByKey("testMtSignalCatch");
        runtimeService.startProcessInstanceByKey("testMtSignalCatch");

        // verify
        assertThat(taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(3);
        assertThat(taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count()).isEqualTo(2);

        // Now, start 1 process instance that fires a signal event (not in tenant context), it should only continue those without tenant
        runtimeService.startProcessInstanceByKey("testMtSignalFiring");
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(0);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(2);

        // Start a process instance that is running in tenant context
        runtimeService.startProcessInstanceByKeyAndTenantId("testMtSignalFiring",
                                                            TEST_TENANT_ID);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(3);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(2);

        // Cleanup
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(),
                                               true);
        }
    }

    public void testSignalThroughApiTenancy() {

        // Deploy process both with and without tenant
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml").deploy();
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml").tenantId(TEST_TENANT_ID).deploy();

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
        assertThat(taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count()).isEqualTo(5);

        // Signal through API (with tenant)
        runtimeService.signalEventReceivedWithTenantId("The Signal", TEST_TENANT_ID);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(0);

        // Signal through API (without tenant)
        runtimeService.signalEventReceived("The Signal");
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(5);

        // Cleanup
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    public void testSignalThroughApiTenancyReversed() { // cause reversing the
        // order of calling DID leave to an error!

        // Deploy process both with and without tenant
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml").deploy();
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml").tenantId(TEST_TENANT_ID).deploy();

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
        assertThat(taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count()).isEqualTo(5);

        // Signal through API (without tenant)
        runtimeService.signalEventReceived("The Signal");
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(0);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(5);

        // Signal through API (with tenant)
        runtimeService.signalEventReceivedWithTenantId("The Signal", TEST_TENANT_ID);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(5);

        // Cleanup
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    public void testSignalAsyncThroughApiTenancy() {

        // Deploy process both with and without tenant
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml").deploy();
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMultiTenancySignals.bpmn20.xml").tenantId(TEST_TENANT_ID).deploy();

        // Start 4 proc instances for the one with a tenant and 5 for the one
        // without tenant
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
        assertThat(taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count()).isEqualTo(5);

        // Signal through API (with tenant)
        runtimeService.signalEventReceivedAsyncWithTenantId("The Signal", TEST_TENANT_ID);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(0);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(0);

        for (Job job : managementService.createJobQuery().list()) {
            managementService.executeJob(job.getId());
        }

        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(0);

        // Signal through API (without tenant)
        runtimeService.signalEventReceivedAsync("The Signal");

        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(0);

        for (Job job : managementService.createJobQuery().list()) {
            managementService.executeJob(job.getId());
        }

        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(4);
        assertThat(taskService.createTaskQuery().taskName("Task after signal").taskWithoutTenantId().count()).isEqualTo(5);

        // Cleanup
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    public void testStartProcessInstanceBySignalTenancy() {

        // Deploy process both with and without tenant
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testStartProcessInstanceBySignalTenancy.bpmn20.xml").deploy();
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testStartProcessInstanceBySignalTenancy.bpmn20.xml").tenantId(TEST_TENANT_ID).deploy();

        // Signaling without tenant
        runtimeService.signalEventReceived("The Signal");
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().count()).isEqualTo(3);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).count()).isEqualTo(0);

        // Signalling with tenant
        runtimeService.signalEventReceivedWithTenantId("The Signal", TEST_TENANT_ID);
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(6);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().count()).isEqualTo(3);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).count()).isEqualTo(3);

        // Start a process instance with a boundary catch (with and without tenant)
        runtimeService.startProcessInstanceByKey("processWithSignalCatch");
        runtimeService.startProcessInstanceByKeyAndTenantId("processWithSignalCatch", TEST_TENANT_ID);

        runtimeService.signalEventReceived("The Signal");
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(11);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().count()).isEqualTo(7);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).count()).isEqualTo(4);

        runtimeService.signalEventReceivedWithTenantId("The Signal", TEST_TENANT_ID);
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(14);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceWithoutTenantId().count()).isEqualTo(7);
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceTenantId(TEST_TENANT_ID).count()).isEqualTo(7);

        // Cleanup
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    public void testStartProcessInstanceByMessageTenancy() {

        // Deploy process both with and without tenant
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMessageTenancy.bpmn20.xml").deploy();
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMessageTenancy.bpmn20.xml").tenantId(TEST_TENANT_ID).deploy();

        // Verify query
        assertThat(repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("My message").count()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("My message").processDefinitionWithoutTenantId().count()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("My message").processDefinitionTenantId(TEST_TENANT_ID).count()).isEqualTo(1);

        // Start a process instance by message without tenant
        runtimeService.startProcessInstanceByMessage("My message");
        runtimeService.startProcessInstanceByMessage("My message");

        assertThat(taskService.createTaskQuery().taskName("My task").count()).isEqualTo(2);
        assertThat(taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count()).isEqualTo(2);
        assertThat(taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(0);

        // Start a process instance by message with tenant
        runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
        runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
        runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
        assertThat(taskService.createTaskQuery().taskName("My task").count()).isEqualTo(5);
        assertThat(taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count()).isEqualTo(2);
        assertThat(taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(3);

        // Cleanup
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    public void testStartProcessInstanceByMessageTenancyReversed() { // same as
        // above, but now reversed

        // Deploy process both with and without tenant
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMessageTenancy.bpmn20.xml").deploy();
        repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testMessageTenancy.bpmn20.xml").tenantId(TEST_TENANT_ID).deploy();

        // Start a process instance by message with tenant
        runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
        runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
        runtimeService.startProcessInstanceByMessageAndTenantId("My message", TEST_TENANT_ID);
        assertThat(taskService.createTaskQuery().taskName("My task").count()).isEqualTo(3);
        assertThat(taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count()).isEqualTo(0);
        assertThat(taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(3);

        // Start a process instance by message without tenant
        runtimeService.startProcessInstanceByMessage("My message");
        runtimeService.startProcessInstanceByMessage("My message");

        assertThat(taskService.createTaskQuery().taskName("My task").count()).isEqualTo(5);
        assertThat(taskService.createTaskQuery().taskName("My task").taskWithoutTenantId().count()).isEqualTo(2);
        assertThat(taskService.createTaskQuery().taskName("My task").taskTenantId(TEST_TENANT_ID).count()).isEqualTo(3);

        // Cleanup
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    // Bug from http://forums.activiti.org/content/callactiviti-tenant-id
    public void testCallActivityWithTenant() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            String tenantId = "apache";

            // deploying both processes. Process 1 will call Process 2
            repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testCallActivityWithTenant-process01.bpmn20.xml").tenantId(tenantId).deploy();
            repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/tenant/TenancyTest.testCallActivityWithTenant-process02.bpmn20.xml").tenantId(tenantId).deploy();

            // Starting Process 1. Process 1 will be executed successfully but
            // when the call to process 2 is made internally it will throw the
            // exception
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("process1",
                                                                                                  null,
                                                                                                  singletonMap("sendFor", "test"),
                                                                                                  tenantId);
            assertThat(processInstance).isNotNull();

            assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey("process2").processInstanceTenantId(tenantId).count()).isEqualTo(1);
            assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey("process2").count()).isEqualTo(1);

            // following line if executed will give activiti object not found
            // exception as the process1 is linked to a tenant id.
            assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> runtimeService.startProcessInstanceByKey("process1"));

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
        for (int i = 0; i < 4; i++) {
            deployTestProcessWithTestTenant(tenant1);
        }

        // Tenant 2 ==> version 2
        for (int i = 0; i < 2; i++) {
            deployTestProcessWithTestTenant(tenant2);
        }

        // No tenant ==> version 3
        for (int i = 0; i < 3; i++) {
            deployTestProcessWithTwoTasksNoTenant();
        }

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(tenant1)
                .latestVersion()
                .singleResult();
        assertThat(processDefinition.getVersion()).isEqualTo(4);

        processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(tenant2)
                .latestVersion()
                .singleResult();
        assertThat(processDefinition.getVersion()).isEqualTo(2);

        processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionWithoutTenantId()
                .latestVersion()
                .singleResult();
        assertThat(processDefinition.getVersion()).isEqualTo(3);

        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().latestVersion().list();
        assertThat(processDefinitions.size()).isEqualTo(3);

        // Verify they have different tenant ids
        int tenant1Count = 0, tenant2Count = 0, noTenantCount = 0;
        for (ProcessDefinition p : processDefinitions) {
            if (p.getTenantId() == null || p.getTenantId().equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
                noTenantCount++;
            } else if (p.getTenantId().equals(tenant1)) {
                tenant1Count++;
            } else if (p.getTenantId().equals(tenant2)) {
                tenant2Count++;
            }
        }
        assertThat(tenant1Count).isEqualTo(1);
        assertThat(tenant2Count).isEqualTo(1);
        assertThat(noTenantCount).isEqualTo(1);
    }
}
