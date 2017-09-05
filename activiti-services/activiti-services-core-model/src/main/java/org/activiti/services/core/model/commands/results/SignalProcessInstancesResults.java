package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SignalProcessInstancesResults extends AbstractCommandResults {

    public SignalProcessInstancesResults(String commandId) {
        super(commandId);
    }

    @JsonCreator
    public SignalProcessInstancesResults(@JsonProperty("id") String id,
                                         @JsonProperty("commandId") String commandId) {
        super(id,
              commandId);
    }
}
