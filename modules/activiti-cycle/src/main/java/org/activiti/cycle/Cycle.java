package org.activiti.cycle;

import org.activiti.cycle.impl.db.CycleServiceDbXStreamImpl;
import org.activiti.cycle.impl.plugin.PluginFinder;


/**
 * TODO: make configurable somehow
 * @author kristin.polenz@camunda.com
 */
public class Cycle {

  public static CycleService getCycleService() {
    PluginFinder.checkPluginInitialization();
    
    return new CycleServiceDbXStreamImpl();
  }
  
}
