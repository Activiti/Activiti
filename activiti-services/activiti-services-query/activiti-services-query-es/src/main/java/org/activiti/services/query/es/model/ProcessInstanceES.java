package org.activiti.services.query.es.model;

import org.activiti.services.query.app.model.ProcessInstance;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "activiti", type = "processInstance")
public class ProcessInstanceES extends ProcessInstance {

}
