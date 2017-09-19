package org.activiti.services.query.es.model;

import java.util.Date;

import org.activiti.services.query.model.Variable;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "activiti", type = "variable", shards = 1)
public class VariableES extends Variable {

	public VariableES(String variableType, String variableName, String processInstanceId, String taskId,
			Date createTime, Date lastUpdatedTime, String executionId, String variableValue) {
		super(variableType, variableName, processInstanceId, taskId, createTime, lastUpdatedTime, executionId,
				variableValue);

	}

	public VariableES() {
		super();
	}

}
