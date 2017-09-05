package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SuspendProcessInstanceResults extends AbstractCommandResults {

    public SuspendProcessInstanceResults(String commandId) {
        super(commandId);
    }

    @JsonCreator
    public SuspendProcessInstanceResults(@JsonProperty("id") String id,
                                         @JsonProperty("commandId") String commandId) {
        super(id,
              commandId);
    }
}
