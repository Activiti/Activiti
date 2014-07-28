package org.activiti.rest.diagram.application;

import org.activiti.rest.diagram.services.ProcessDefinitionDiagramLayoutResource;
import org.activiti.rest.diagram.services.ProcessInstanceHighlightsResource;
import org.restlet.routing.Router;

public class DiagramServicesInit {

  public static void attachResources(Router router) {
    router.attach("/process-instance/{processInstanceId}/highlights", ProcessInstanceHighlightsResource.class);
    router.attach("/process-instance/{processInstanceId}/diagram-layout", ProcessDefinitionDiagramLayoutResource.class);
    router.attach("/process-definition/{processDefinitionId}/diagram-layout", ProcessDefinitionDiagramLayoutResource.class);
  }
}
