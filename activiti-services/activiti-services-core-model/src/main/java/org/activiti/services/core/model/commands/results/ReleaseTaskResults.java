package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReleaseTaskResults extends AbstractCommandResults {

    public ReleaseTaskResults(String commandId) {
        super(commandId);
    }

    @JsonCreator
    public ReleaseTaskResults(@JsonProperty("id") String id,
                              @JsonProperty("commandId") String commandId) {
        super(id,
              commandId);
    }
}
