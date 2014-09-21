package org.activiti.spring.boot.actuate.endpoint;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricDetail;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "endpoints.activiti")
public class ProcessEngineEndpoint extends AbstractEndpoint<List<HistoricDetail>> {

    private final ProcessEngine processEngine;

    public ProcessEngineEndpoint(ProcessEngine processEngine) {
        super("activiti");
        this.processEngine = processEngine;
    }

    @Override
    public List<HistoricDetail> invoke() {
        return this.processEngine.getHistoryService()
                .createHistoricDetailQuery()
                .orderByTime()
                .asc()
                .list();
    }
}

    /*  ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery().processDefinitionKey(pd).singleResult();
        ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
        InputStream is = processDiagramGenerator
                .generatePngDiagram(repositoryService
                        .getBpmnModel(processDefinition.getId()));

        return new InputStreamResource(is);
        */