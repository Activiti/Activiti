package org.activiti.spring.boot.actuate.endpoint;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricDetail;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Registers a Boot Actuator endpoint that provides information on the
 * running process instance and renders BPMN diagrams of the deployed processes.
 *
 * @author Josh Long
 */
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
