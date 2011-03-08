package org.activiti.rest.api.cycle;

import java.util.Map;

import org.activiti.cycle.impl.processsolution.event.ImplementationDoneEvent;
import org.activiti.cycle.impl.processsolution.event.SpecificationDoneEvent;
import org.activiti.cycle.impl.processsolution.event.TestingDoneEvent;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.ProcessSolutionState;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiRequestObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Webscript for updating a process solution
 * 
 * @author daniel.meyer@camunda.com
 */
public class ProcessSolutionPut extends ActivitiCycleWebScript {

  @Override
  void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    ActivitiRequestObject obj = req.getBody();
    String processSolutionId = req.getMandatoryString(obj, "processSolutionId");
    String state = req.getMandatoryString(obj, "state");
    // for the moment just making this synchronized to make sure events are not
    // fired concurrently
    synchronized (ProcessSolutionPut.class) {
      try {
        ProcessSolution processSolution = processSolutionService.getProcessSolutionById(processSolutionId);
        if (processSolution == null) {
          throw new RuntimeException("ProcessSolution with id '" + processSolutionId + "' not found.");
        }

        ProcessSolutionState newState = ProcessSolutionState.valueOf(state);
        if (!newState.equals(processSolution.getState())) {
          switch (newState) {
          case IN_IMPLEMENTATION:
            eventService.fireEvent(new SpecificationDoneEvent(processSolution));
            break;
          case IN_TESTING:
            eventService.fireEvent(new ImplementationDoneEvent(processSolution));
            break;
          default:
            eventService.fireEvent(new TestingDoneEvent(processSolution));
          }
        }
        model.put("result", "success");

      } catch (Exception e) {
        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not update ProcessSolution. " + e.getMessage(), e);
      }
    }
  }
}
