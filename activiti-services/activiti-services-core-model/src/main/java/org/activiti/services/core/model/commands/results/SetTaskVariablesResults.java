package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetTaskVariablesResults extends AbstractCommandResults {

    public SetTaskVariablesResults(String commandId) {
        super(commandId);
    }

    @JsonCreator
    public SetTaskVariablesResults(@JsonProperty("id") String id,
                                   @JsonProperty("commandId") String commandId) {
        super(id,
              commandId);
    }
}
