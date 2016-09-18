package org.activiti.examples.runtime;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.junit.Ignore;

/**
 * This test shows that bpmn endless loop with activiti6 is not only fiction
 */
@Ignore
public class StandardAgendaFailingTest extends PluggableActivitiTestCase {

    @Ignore("Endless loop with the standard agenda implementation can run 'forever'.")
    @Deployment(resources = "org/activiti/examples/runtime/WatchDogAgendaTest-endlessloop.bpmn20.xml")
    public void ignoreStandardAgendaWithEndLessLoop() {
        this.runtimeService.startProcessInstanceByKey("endlessloop");
    }

}
