package org.activiti.rest.api.cycle.session;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.context.CycleContext;

/**
 * Wrapper for {@link HttpSession} Context
 * 
 * @author daniel.meyer@camunda.com
 */
public class HttpSessionContext implements CycleContext {

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

  public Set<String> getKeySet() {
    HashSet<String> result = new HashSet<String>();
    for (@SuppressWarnings("unchecked")
    Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
      result.add(e.nextElement());
    }
    return result;
  }

  public Map<String, Object> getValues() {
    HashMap<String, Object> result = new HashMap<String, Object>();
    for (@SuppressWarnings("unchecked")
    Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
      String key = e.nextElement();
      result.put(key, session.getAttribute(key));
    }
    return result;
  }

}
