package org.activiti.services.core.model.commands.results;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.activiti.services.api.commands.results.CommandResults;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(
                        value = StartProcessInstanceResults.class,
                        name = "StartProcessInstanceResults"),
                @JsonSubTypes.Type(
                        value = SuspendProcessInstanceResults.class,
                        name = "SuspendProcessInstanceResults"),
                @JsonSubTypes.Type(
                        value = ActivateProcessInstanceResults.class,
                        name = "ActivateProcessInstanceResults"),
                @JsonSubTypes.Type(
                        value = SignalProcessInstancesResults.class,
                        name = "SignalProcessInstancesResults"),
                @JsonSubTypes.Type(
                        value = ClaimTaskResults.class,
                        name = "ClaimTaskResults"),
                @JsonSubTypes.Type(
                        value = CompleteTaskResults.class,
                        name = "CompleteTaskResults"),
                @JsonSubTypes.Type(
                        value = ReleaseTaskResults.class,
                        name = "ReleaseTaskResults"),
                @JsonSubTypes.Type(
                        value = SetTaskVariablesResults.class,
                        name = "SetTaskVariablesResults")
        }
)
public abstract class AbstractCommandResults implements CommandResults {

    private String id;
    private String commandId;

    public AbstractCommandResults(String commandId) {
        this.id = UUID.randomUUID().toString();
        this.commandId = commandId;
    }

    public AbstractCommandResults(String id,
                                  String commandId) {
        this.id = id;
        this.commandId = commandId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }
}
