package org.activiti.cycle.impl.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.event.CycleCompensatingEventListener;
import org.activiti.cycle.event.CycleEventListener;
import org.activiti.cycle.impl.event.CycleEvents;
import org.activiti.cycle.service.CycleEventService;

/**
 * Default implementation of the {@link CycleEventService} interface
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleEventServiceImpl implements CycleEventService {

  Logger log = Logger.getLogger(CycleEventService.class.getName());

  public <T> void fireEvent(T event) {
    Exception exception = null;
    List<CycleEventListener<T>> successfulEventListeners = new ArrayList<CycleEventListener<T>>();
    CycleEvents cycleEvents = CycleComponentFactory.getCycleComponentInstance(CycleEvents.class, CycleEvents.class);
    for (CycleEventListener<T> eventListener : cycleEvents.getEventListeners((Class<T>) event.getClass())) {
      try {
        eventListener.onEvent(event);
        successfulEventListeners.add(eventListener);
      } catch (Exception e) {
        log.log(Level.SEVERE, "Error while invoking EventListener '" + eventListener.getClass().getName() + "' with event '" + event + "': " + e.getMessage(),
                e);
        exception = e;
        break;
      }
    }
    // try to compensate
    if (exception != null) {
      for (CycleEventListener<T> cycleEventListener : successfulEventListeners) {
        if (cycleEventListener instanceof CycleCompensatingEventListener) {
          CycleCompensatingEventListener<T> compensatingListener = (CycleCompensatingEventListener<T>) cycleEventListener;
          try {
            compensatingListener.compensateEvent(event);
          } catch (Exception e) {
            log.log(Level.SEVERE,
                    "Error while compensating EventListener '" + compensatingListener.getClass().getName() + "' with event '" + event + "': " + e.getMessage(),
                    e);
            // let this one pass, error during compensation

          }
        }
        throw new RuntimeException("Error on" + event + ": " + exception.getMessage(), exception);
      }
    }
  }

}
