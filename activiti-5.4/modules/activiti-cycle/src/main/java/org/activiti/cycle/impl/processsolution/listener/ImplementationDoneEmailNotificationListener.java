package org.activiti.cycle.impl.processsolution.listener;

import java.io.StringWriter;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.event.CycleEventListener;
import org.activiti.cycle.impl.processsolution.event.ImplementationDoneEvent;

/**
 * Listener sending an email when the implementation for a given process
 * solution is completed.
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class ImplementationDoneEmailNotificationListener extends AbstractProcessSolutionStateEmailListener<ImplementationDoneEvent> implements
        CycleEventListener<ImplementationDoneEvent> {

  protected String getSubject(ImplementationDoneEvent event) {
    return "Implementation done in " + event.getProcessSolution().getLabel();
  }

  protected String getMessage(ImplementationDoneEvent event) {
    StringWriter writer = new StringWriter();
    writer.append("Dear collaborator in project " + event.getProcessSolution().getLabel() + ".");
    writer.append("<br />");
    writer.append("<br />");
    writer.append("The project has completed the <em>implementation</em> phase and is now in <em>testing</em>.");
    writer.append("<br />");
    writer.append("<br />");

    String psConnectorId = "ps-" + event.getProcessSolution().getId();
    String psLabel = event.getProcessSolution().getLabel();
    writer.append("Go to <a href='http://localhost:8080/activiti-cycle/service/#event=updateArtifactView/activeNavigationTabIndex/0/activeArtifactViewTabIndex/0/connectorId/" + psConnectorId + "/nodeId/%252F/label/" + psLabel + "'>Cycle Process Solution Homepage</a>");    

    writer.append("<br />");
    writer.append("<br />");
    writer.append("With best regards from your Activiti Cycle.");
    return writer.toString();
  }
}
