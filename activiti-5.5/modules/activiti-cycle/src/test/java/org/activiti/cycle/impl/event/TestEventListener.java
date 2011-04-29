package org.activiti.cycle.impl.event;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.event.CycleEventListener;

@CycleComponent(context = CycleContextType.NONE)
public class TestEventListener implements CycleEventListener<TestEvent> {

  static boolean IS_INVOKED = false;

  public void onEvent(TestEvent event) {
    IS_INVOKED = true;
  }
}
