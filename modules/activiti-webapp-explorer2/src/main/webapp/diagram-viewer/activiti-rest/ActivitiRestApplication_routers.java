	router.attach("/process-instance/{processInstanceId}/highlights", ProcessInstanceHighLightsResource.class);
    router.attach("/process-instance/{processInstanceId}/diagram-layout", ProcessDefinitionDiagramLayoutResource.class);
    router.attach("/process-definition/{processDefinitionId}/diagram-layout", ProcessDefinitionDiagramLayoutResource.class);
    