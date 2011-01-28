package org.activiti.cycle.impl;

import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleMapContext;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.junit.After;
import org.junit.Before;

/**
 * Abstract base class for Activiti Cycle tests. Sets up the cycle context
 * infrastructure.
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class ActivitiCycleTest {

  @Before
  public void setupCycleInfrastructure() {
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

  @After
  public void tearDownCycleInfrastructure() {
    CycleApplicationContext.setWrappedContext(null);
    CycleSessionContext.clearContext();
    CycleRequestContext.clearContext();
  }

}
