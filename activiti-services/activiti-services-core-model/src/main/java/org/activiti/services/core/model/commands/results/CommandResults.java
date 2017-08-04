package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "resultType")
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
        }
)
public interface CommandResults {

    String getId();

    String getCommandId();
}
