package org.activiti.rest.api.cycle.session;

import org.activiti.rest.api.cycle.session.CycleHttpSession.CycleRequestFilter;
import org.activiti.rest.util.ActivitiRequest;
import org.restlet.engine.Engine;

/**
 * 
 * 
 * @author daniel.meyer@camunda.com
 */
public class RestletThreadLocalReaper implements CycleRequestFilter {

  public void beforeRequest(ActivitiRequest req) {
  }

  public void afterRequest(ActivitiRequest req) {
    // clear Restlet thread-local variables at the end of each request. 
    Engine.clearThreadLocalVariables();
  }

}
