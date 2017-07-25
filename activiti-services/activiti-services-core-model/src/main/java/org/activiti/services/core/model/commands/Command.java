package org.activiti.services.core.model.commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "commandType")
@JsonSubTypes({@JsonSubTypes.Type(
        value = StartProcessInstanceCmd.class,
        name = "StartProcessInstanceCmd"), 
    @JsonSubTypes.Type(
        value = SignalProcessInstancesCmd.class,
        name = "SignalProcessInstancesCmd"), 
    @JsonSubTypes.Type(
        value = CompleteTaskCmd.class,
        name = "CompleteTaskCmd")
})
public interface Command {

}
