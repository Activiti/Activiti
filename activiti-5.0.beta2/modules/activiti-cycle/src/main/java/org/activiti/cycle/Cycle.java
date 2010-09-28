package org.activiti.cycle;

import org.activiti.cycle.impl.conf.CycleServiceDbXStreamImpl;


/**
 * TODO: make configurable somehow
 * @author polenz
 */
public class Cycle {

  public static CycleService getCycleService() {
    return new CycleServiceDbXStreamImpl();
  }
  
}
