package org.activiti.services.core.model.commands;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.activiti.services.api.commands.Command;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(
                        value = StartProcessInstanceCmd.class,
                        name = "StartProcessInstanceCmd"),
                @JsonSubTypes.Type(
                        value = SignalProcessInstancesCmd.class,
                        name = "SignalProcessInstancesCmd"),
                @JsonSubTypes.Type(
                        value = SuspendProcessInstanceCmd.class,
                        name = "SuspendProcessInstanceCmd"),
                @JsonSubTypes.Type(
                        value = ActivateProcessInstanceCmd.class,
                        name = "ActivateProcessInstanceCmd"),
                @JsonSubTypes.Type(
                        value = ClaimTaskCmd.class,
                        name = "ClaimTaskCmd"),
                @JsonSubTypes.Type(
                        value = ReleaseTaskCmd.class,
                        name = "ReleaseTaskCmd"),
                @JsonSubTypes.Type(
                        value = CompleteTaskCmd.class,
                        name = "CompleteTaskCmd"),
                @JsonSubTypes.Type(
                        value = SetTaskVariablesCmd.class,
                        name = "SetTaskVariablesCmd")
        })
public abstract class AbstractCommand implements Command {

    private final String id;

    public AbstractCommand() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }
}
