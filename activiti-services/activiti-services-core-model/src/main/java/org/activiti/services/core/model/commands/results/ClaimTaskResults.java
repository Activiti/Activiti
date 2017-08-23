package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClaimTaskResults extends AbstractCommandResults {

    public ClaimTaskResults(String commandId) {
        super(commandId);
    }

    @JsonCreator
    public ClaimTaskResults(@JsonProperty("id") String id,
                            @JsonProperty("commandId") String commandId) {
        super(id,
              commandId);
    }
}
