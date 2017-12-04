package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

public class BoundarySignalEventAwareStartEventActivityBehavior extends FlowNodeActivityBehavior {
  
  private final ActivityBehavior initialActivity;
  private final ActivityImpl activity;
  
  public BoundarySignalEventAwareStartEventActivityBehavior(ActivityImpl delegate) {
    this.initialActivity = delegate.getActivityBehavior();
    this.activity = delegate.getParentActivity();
  }
  
  @Override
  public void execute(ActivityExecution execution) throws Exception {

    // Check any boundary event signal activity matching registered signal event in process instance execution scope
    for(BoundaryEventActivityBehavior boundaryEventActivityBehavior: getBoundaryEventActivities(activity)) {
      SignalEventDefinition signalEventDef = findSignalEventDefinition(boundaryEventActivityBehavior);
      
      if(signalEventDef != null) {
        if(Context.getCommandContext().getSignalEventSessionManager()
            .hasThrowSignalEventForExecution(execution, signalEventDef.getSignalRef())) 
        {
          // Execute outgoing boundary signal event activity
          boundaryEventActivityBehavior.execute(execution);

          return;
        }
      }
    }        
    
    this.initialActivity.execute(execution);
  }
  
  protected List<BoundaryEventActivityBehavior> getBoundaryEventActivities(ActivityImpl activity) {
    List<BoundaryEventActivityBehavior> result = new ArrayList<BoundaryEventActivityBehavior>();

    List<ActivityImpl> children = activity.getActivities();
    
    for(ActivityImpl child: children) {
      if(child.getActivityBehavior() instanceof BoundaryEventActivityBehavior) {
        BoundaryEventActivityBehavior boundaryEventActivity = (BoundaryEventActivityBehavior) child.getActivityBehavior();
        result.add(boundaryEventActivity);
      }
    }
    
    return result;
  }

  protected SignalEventDefinition findSignalEventDefinition(BoundaryEventActivityBehavior boundaryEventActivityBehavior) {
    SignalEventDefinition signalEventDef = null;
    BoundaryEvent boundaryEvent = boundaryEventActivityBehavior.getBoundaryEvent();
    
    if(boundaryEvent != null) {
      for(EventDefinition eventDef : boundaryEvent.getEventDefinitions()) {
        if(eventDef instanceof SignalEventDefinition) {
          signalEventDef = (SignalEventDefinition) eventDef;
        }
      }
    }
    
    return signalEventDef;
    
  }      
}
