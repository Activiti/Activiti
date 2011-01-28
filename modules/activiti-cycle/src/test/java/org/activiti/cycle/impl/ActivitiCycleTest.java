package org.activiti.cycle.impl;

import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleMapContext;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.engine.ProcessEngine;
import org.junit.After;
import org.junit.Before;

/**
 * Abstract base class for Activiti Cycle tests. Sets up the cycle context
 * infrastructure and (if needed) the {@link ProcessEngine} and database.
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class ActivitiCycleTest {

  @Before
  public void setup() throws Exception {
    setupCycleContextInfrastructure();
  }

  @After
  public void teardown() throws Exception {
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
