package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivateProcessInstanceResults extends AbstractCommandResults {

    public ActivateProcessInstanceResults(String commandId) {
        super(commandId);
    }

    @JsonCreator
    public ActivateProcessInstanceResults(@JsonProperty("id") String id,
                                          @JsonProperty("commandId") String commandId) {
        super(id,
              commandId);
    }
}
