package org.activiti.diagram.rest.application;

import org.activiti.diagram.rest.services.ProcessDefinitionDiagramLayoutResource;
import org.activiti.diagram.rest.services.ProcessInstanceHighlightsResource;
import org.restlet.routing.Router;

public class DiagramServicesInit {

  public static void attachResources(Router router) {
    router.attach("/process-instance/{processInstanceId}/highlights", ProcessInstanceHighlightsResource.class);
    router.attach("/process-instance/{processInstanceId}/diagram-layout", ProcessDefinitionDiagramLayoutResource.class);
    router.attach("/process-definition/{processDefinitionId}/diagram-layout", ProcessDefinitionDiagramLayoutResource.class);
  }
}
