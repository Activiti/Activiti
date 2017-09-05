package org.activiti.services.core.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ProcessDefinitionVariable extends JsonDeserializer<Set<ProcessDefinitionVariable>> {

    @JsonProperty("variableName")
    private String variableName;
    @JsonProperty("variableType")
    private String variableType;

    public ProcessDefinitionVariable() {
    };

    @JsonCreator
    public ProcessDefinitionVariable(String variableName, String variableType) {
        this.variableName = variableName;
        this.variableType = variableType;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableType() {
        return variableType;
    }

    @Override
    public Set<ProcessDefinitionVariable> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
                                                                                                   JsonProcessingException {

        Set<ProcessDefinitionVariable> variables = new HashSet<ProcessDefinitionVariable>();
        ObjectCodec oc = jp.getCodec();
        JsonNode nodes = oc.readTree(jp);

        for (int i = 0; i < nodes.size(); i++) {
            ProcessDefinitionVariable variable = new ProcessDefinitionVariable(nodes.get(i)
                                                                                    .get("variableName")
                                                                                    .asText(),
                                                                               nodes.get(i)
                                                                                    .get("variableType")
                                                                                    .asText());
            variables.add(variable);
        }

        return variables;
    }
}
