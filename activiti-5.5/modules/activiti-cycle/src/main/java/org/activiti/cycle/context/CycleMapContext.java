package org.activiti.cycle.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * a simple cycle context implementation based on a {@link Map}.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleMapContext implements CycleContext {

  private HashMap<String, Object> map = new HashMap<String, Object>();

  public void set(String key, Object value) {
    map.put(key, value);
  }

  public Object get(String key) {
    return map.get(key);
  }

  public Set<String> getKeySet() {
    return map.keySet();
  }

  public Map<String, Object> getValues() {
    return new HashMap<String, Object>(map);
  }
}
