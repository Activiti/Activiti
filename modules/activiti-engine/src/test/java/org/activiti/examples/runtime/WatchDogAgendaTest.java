package org.activiti.examples.runtime;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This class shows an example of configurable agenda usage.
 */
public class WatchDogAgendaTest extends ResourceActivitiTestCase {

    public WatchDogAgendaTest() {
        super(WatchDogAgendaTest.class.getName().replace(".", File.separator)+".activiti.cfg.xml");
    }

    @Deployment(resources = "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
    public void testWatchDogWithOneTaskProcess() {
        this.runtimeService.startProcessInstanceByKey("oneTaskProcess");
        Task task = this.taskService.createTaskQuery().singleResult();
        this.taskService.complete(task.getId());
        assertThat(this.runtimeService.createProcessInstanceQuery().count(), is(0L));
    }

    @Deployment(resources = "org/activiti/examples/runtime/WatchDogAgendaTest-endlessloop.bpmn20.xml")
    public void testWatchDogWithEndLessLoop() {
        try {
            this.runtimeService.startProcessInstanceByKey("endlessloop");
            fail("ActivitiException with 'WatchDog limit exceeded.' message expected.");
        } catch (ActivitiException e) {
            if (!"WatchDog limit exceeded.".equals(e.getMessage())) {
                fail("Unexpected exception " + e);
            }
        }
     }

}
