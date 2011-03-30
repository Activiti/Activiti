package org.activiti.cycle.impl.event;

import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.event.CycleEventListener;
import org.activiti.cycle.impl.ActivitiCycleDbAwareTest;

/**
 * Tests the {@link CycleEvents}-Component
 * 
 * @author daniel.meyer@camunda.com
 */
public class EventsTest extends ActivitiCycleDbAwareTest {

  public void testInitialization() {
    CycleEvents events = CycleComponentFactory.getCycleComponentInstance(CycleEvents.class, CycleEvents.class);
    Set<CycleEventListener<TestEvent>> listeners = events.getEventListeners(TestEvent.class);
    assertTrue(listeners.size() == 1);
  }
}
