package org.activiti.spring.boot;


public class ProcessEngineProcessesMetric {

    /*

// todo maybe we can contribute some sort of HealthIndicator based on Activiti?
class ActivitiDiagramController {
    @Autowired
    RepositoryService repositoryService;

    @RequestMapping(value = "/processes/diagrams/{pd}", produces = MediaType.IMAGE_PNG_VALUE)
    Resource renderProcessDiagram(@PathVariable String pd) {
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery().processDefinitionKey(pd).singleResult();
        ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
        InputStream is = processDiagramGenerator
                .generatePngDiagram(repositoryService
                        .getBpmnModel(processDefinition.getId()));

        return new InputStreamResource(is);

    }

}  */
}
