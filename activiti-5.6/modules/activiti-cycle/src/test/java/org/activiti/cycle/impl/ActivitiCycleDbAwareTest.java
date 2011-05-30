package org.activiti.cycle.impl;

import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleMapContext;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

public abstract class ActivitiCycleDbAwareTest extends PluggableActivitiTestCase {

  @Override
  protected void setUp() throws Exception {
    setupCycleContextInfrastructure();
  }

  @Override
  protected void tearDown() throws Exception {
    tearDownCycleContextInfrastructure();
  }

  protected void setupCycleContextInfrastructure() {
    CycleApplicationContext.setWrappedContext(new CycleMapContext());
    CycleSessionContext.setContext(new CycleMapContext());
    CycleRequestContext.setContext(new CycleMapContext());
    populateApplicationContext();
    populateSessionContext();
    populateRequestContext();
  }

  protected void populateApplicationContext() {
  }

  protected void populateSessionContext() {
    CycleSessionContext.set("cuid", "testuser");
  }

  protected void populateRequestContext() {
  }

  protected void tearDownCycleContextInfrastructure() {
    CycleApplicationContext.setWrappedContext(null);
    CycleSessionContext.clearContext();
    CycleRequestContext.clearContext();
  }

}
