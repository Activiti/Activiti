package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CompleteTaskResults extends AbstractCommandResults {

    public CompleteTaskResults(String commandId) {
        super(commandId);
    }

    @JsonCreator
    public CompleteTaskResults(@JsonProperty("id") String id,
                               @JsonProperty("commandId") String commandId) {
        super(id,
              commandId);
    }
}
