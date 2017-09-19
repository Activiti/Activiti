package org.activiti.services.query.es.model;

import org.activiti.services.query.model.Task;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "activiti", type = "task", shards = 1)
public class TaskES extends Task {

}
