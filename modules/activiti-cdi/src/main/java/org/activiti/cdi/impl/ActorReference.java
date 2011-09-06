package org.activiti.cdi.impl;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.activiti.cdi.Actor;

/**
 * this allows to safely retrieve the current {@link Actor}, even if the sessionScope is not active.
 * 
 * @author Daniel Meyer
 */
public class ActorReference {
  
  @Inject Instance<Actor> actorInstance;
  
  public Actor getActor() {
    return actorInstance.get();
  }
  
  public boolean isAvailable() {
    try { // TODO: find a better way to check this 
      actorInstance.get().getActorId();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
