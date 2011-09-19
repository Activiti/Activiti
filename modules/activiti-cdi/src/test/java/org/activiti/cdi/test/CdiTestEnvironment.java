package org.activiti.cdi.test;

import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * Sets up the default environment for activiti-cdi tests
 * 
 * @author Daniel Meyer
 * 
 */
public class CdiTestEnvironment extends AbstractCdiTestEnvironment {
  
  private static Logger log = Logger.getLogger(CdiTestEnvironment.class.getName());
 
  protected WeldContainer weldContainer;
  protected BeanManager beanManager;
  protected Weld weld;

  public void setup() throws Exception {   
    setupCdi();   
  }
  
  public void teardown() throws Exception {   
    tearDownCdi();   
  }
  
  protected void setupCdi() {
    log.info("=========== setting up cdi / weld");
    // bootstrap the CDI container
    weld = new Weld();    
    weldContainer = weld.initialize();    
    beanManager = weldContainer.getBeanManager();
    BeanManagerLookup.localInstance = beanManager;
  }
  
  protected void tearDownCdi() {
    log.info("=========== shutting down cdi / weld");
    weld.shutdown();
  }

  
  public WeldContainer getWeldContainer() {
    return weldContainer;
  }
  
  public BeanManager getBeanManager() {
    return beanManager;
  }
  
  public Weld getWeld() {
    return weld;
  }
  
}
