package org.activiti.cdi.test;

import java.util.logging.Logger;

/**
 * 
 * @author Daniel Meyer
 */
public abstract class AbstractCdiTestEnvironment {

  private static Logger log = Logger.getLogger(AbstractCdiTestEnvironment.class.getName());

  public void setUpEnvironment() throws Exception {
    log.info("################################### starting cdi test environment setup");
    setup();
    log.info("################################### setup of cdi test environment complete");
  }

  protected abstract void setup() throws Exception;

  public void tearDownEnvironment() throws Exception {
    log.info("################################### tearing down cdi test environment");
    teardown();
    log.info("################################### teardown of cdi test environment complete");
  }

  protected abstract void teardown() throws Exception;

}
