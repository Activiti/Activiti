package org.activiti.cdi.test.impl.event;

import static org.junit.Assert.assertEquals;

import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

public class MultiInstanceServiceTaskEventTest extends CdiActivitiTestCase {

  @Test
  @Deployment(resources = { "org/activiti/cdi/test/impl/event/MultiInstanceServiceTaskEvent.bpmn20.xml" })
  public void testReceiveAll() {

    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertEquals(0, listenerBean.getStartActivityService1WithLoopCounter());
    assertEquals(0, listenerBean.getEndActivityService1());
    assertEquals(0, listenerBean.getStartActivityService2WithLoopCounter());
    assertEquals(0, listenerBean.getEndActivityService2());

    // start the process
    runtimeService.startProcessInstanceByKey("process1");

    // assert
    assertEquals(1, listenerBean.getTakeTransitiont1());
    assertEquals(1, listenerBean.getTakeTransitiont2());
    assertEquals(1, listenerBean.getTakeTransitiont3());
    assertEquals(2, listenerBean.getStartActivityService1WithLoopCounter());
    assertEquals(3, listenerBean.getStartActivityService2WithLoopCounter());
    assertEquals(2, listenerBean.getEndActivityService1());
    // Uncomment the following line once ACT-1271 is also solved for parallel multi-instance tasks
    //assertEquals(3, listenerBean.getEndActivityService2());
  }
}
