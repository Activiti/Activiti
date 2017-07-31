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
                        name = "StartProcessInstanceResults")
        }
)
public interface CommandResults {

    String getId();

    String getCommandId();
}
