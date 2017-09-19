package org.activiti.services.query.es.model;

import org.activiti.services.query.model.ProcessInstance;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "activiti", type = "processInstance", shards = 1)
public class ProcessInstanceES extends ProcessInstance {

}
