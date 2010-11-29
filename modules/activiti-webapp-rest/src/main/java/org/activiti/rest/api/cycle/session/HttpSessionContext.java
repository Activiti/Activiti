package org.activiti.rest.api.cycle.session;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleSessionContext.Context;


public class HttpSessionContext implements Context {
  
  private final HttpSession session;
  
  public HttpSessionContext(HttpSession session) {
    this.session = session;
  }

  public void set(String key, Object value) {
    session.setAttribute(key, value);    
  }

  public Object get(String key) {
    return session.getAttribute(key);
  }

}
