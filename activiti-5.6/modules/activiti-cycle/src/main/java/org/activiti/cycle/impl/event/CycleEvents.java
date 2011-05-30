package org.activiti.cycle.impl.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.event.CycleEventListener;

/**
 * {@link CycleContextType#APPLICATION}-scoped component for managing events.
 * 
 * @author daniel.meyer@camunda.com
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@CycleComponent(context = CycleContextType.APPLICATION)
public class CycleEvents {

  protected Map<Class< ? >, Set<CycleEventListener< ? >>> eventListenerMap = null;

  public <T> Set<CycleEventListener<T>> getEventListeners(Class<T> forEvent) {
    Set<CycleEventListener<T>> resultSet = new HashSet<CycleEventListener<T>>();
    init();
    if (eventListenerMap.get(forEvent) != null) {
      for (CycleEventListener< ? > cycleEventListener : eventListenerMap.get(forEvent)) {
        resultSet.add((CycleEventListener<T>) cycleEventListener);
      }
    }
    return resultSet;
  }

  protected void init() {
    if (eventListenerMap == null) {
      synchronized (this) {
        if (eventListenerMap == null) {
          eventListenerMap = new HashMap<Class< ? >, Set<CycleEventListener< ? >>>();
          Set<Class<CycleEventListener>> allImplementations = CycleComponentFactory.getAllImplementations(CycleEventListener.class);
          for (Class<CycleEventListener> class1 : allImplementations) {
            Type[] parameterizedTypes = class1.getGenericInterfaces();
            for (Type type : parameterizedTypes) {
              if (!(type instanceof ParameterizedType)) {
                continue;
              }
              ParameterizedType parameterizedType = (ParameterizedType) type;
              for (Type eventType : parameterizedType.getActualTypeArguments()) {
                if (!(eventType instanceof Class)) {
                  continue;
                }
                // found the event type
                Class< ? > eventClass = (Class< ? >) eventType;
                Set<CycleEventListener< ? >> listenersForThisType = eventListenerMap.get(eventClass);
                if (listenersForThisType == null) {
                  listenersForThisType = new HashSet<CycleEventListener< ? >>();
                  eventListenerMap.put(eventClass, listenersForThisType);
                }
                listenersForThisType.add(CycleApplicationContext.get(class1));

              }
            }
          }
        }
      }
    }
  }
}
