package org.activiti.api.task.model.builders;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.task.model.payloads.SetTaskVariablesPayload;

public class SetTaskVariablesPayloadBuilder {

    private String taskId;
    private Map<String, Object> variables;

    public SetTaskVariablesPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public SetTaskVariablesPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }
    
    public SetTaskVariablesPayloadBuilder withVariable(String name, Object value) {
		if (this.variables == null) {
			this.variables = new HashMap<>();
		}
		this.variables.put(name, 
						   value);
		return this;
	}    

    public SetTaskVariablesPayload build() {
        return new SetTaskVariablesPayload(taskId,
                                           variables);
    }
}
