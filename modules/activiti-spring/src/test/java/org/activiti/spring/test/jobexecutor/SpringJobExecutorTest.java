package org.activiti.spring.test.jobexecutor;

import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.impl.test.CleanTestExecutionListener;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Pablo Ganga
 * @author Joram Barrez
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(CleanTestExecutionListener.class)
@ContextConfiguration("classpath:org/activiti/spring/test/components/SpringjobExecutorTest-context.xml")
public class SpringJobExecutorTest extends SpringActivitiTestCase {

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;

    @Test
    public void testHappyJobExecutorPath() throws Exception {

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("process1");
        assertNotNull(instance);
        waitForTasksToExpire();

        List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
        assertTrue(activeTasks.isEmpty());
    }

    @Test
    public void testRollbackJobExecutorPath() throws Exception {

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("errorProcess1");
        assertNotNull(instance);
        waitForTasksToExpire();

        List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
        assertTrue(activeTasks.size() == 1);
    }

    private void waitForTasksToExpire() throws Exception {
        Thread.sleep(10000L);
    }

}
