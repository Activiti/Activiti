package org.activiti.spring.test.jpa;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author Frederik Heremans
 */
@ContextConfiguration(locations = "JPASpringTest-context.xml")
public class JpaTest extends SpringActivitiTestCase {

    public void testJpaVariableHappyPath() {
        before();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("customerName", "John Doe");
        variables.put("amount", 15000L);

        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("LoanRequestProcess", variables);

        // Variable should be present containing the loanRequest created by the
        // spring bean
        Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
        assertNotNull(value);
        assertTrue(value instanceof LoanRequest);
        LoanRequest request = (LoanRequest) value;
        assertEquals("John Doe", request.getCustomerName());
        assertEquals(15000L, request.getAmount().longValue());
        assertFalse(request.isApproved());

        // We will approve the request, which will update the entity
        variables = new HashMap<String, Object>();
        variables.put("approvedByManager", Boolean.TRUE);

        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId()).singleResult();
        assertNotNull(task);
        taskService.complete(task.getId(), variables);

        // If approved, the processsInstance should be finished, gateway based on loanRequest.approved value
        assertEquals(0, runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstance.getId()).count());

        // Cleanup
        deleteDeployments();
    }

    public void testJpaVariableDisapprovalPath() {

        before();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("customerName", "Jane Doe");
        variables.put("amount", 50000);

        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("LoanRequestProcess", variables);

        // Variable should be present containing the loanRequest created by the
        // spring bean
        Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
        assertNotNull(value);
        assertTrue(value instanceof LoanRequest);
        LoanRequest request = (LoanRequest) value;
        assertEquals("Jane Doe", request.getCustomerName());
        assertEquals(50000L, request.getAmount().longValue());
        assertFalse(request.isApproved());

        // We will disapprove the request, which will update the entity
        variables = new HashMap<String, Object>();
        variables.put("approvedByManager", Boolean.FALSE);

        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId()).singleResult();
        assertNotNull(task);
        taskService.complete(task.getId(), variables);

        runtimeService.getVariable(processInstance.getId(), "loanRequest");
        request = (LoanRequest) value;
        assertFalse(request.isApproved());

        // If disapproved, an extra task will be available instead of the process
        // ending
        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId()).singleResult();
        assertNotNull(task);
        assertEquals("Send rejection letter", task.getName());

        // Cleanup
        deleteDeployments();
    }

    protected void before() {
        String[] defs = {"org/activiti/spring/test/jpa/JPASpringTest.bpmn20.xml"};
        for (String pd : defs) {
            repositoryService.createDeployment().addClasspathResource(pd).deploy();
        }
    }

    protected void deleteDeployments() {
        for (Deployment deployment : repositoryService.createDeploymentQuery()
                .list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }
}
