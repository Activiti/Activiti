package org.activiti.services.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ProcessDefinitionUserTask extends JsonDeserializer<List<ProcessDefinitionUserTask>> {

    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("taskDocumentation")
    private String taskDocumentation;

    public ProcessDefinitionUserTask() {
    }

    @JsonCreator
    public ProcessDefinitionUserTask(String name, String documentation) {
        taskName = name;
        taskDocumentation = documentation;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDocumentation() {
        return taskDocumentation;
    }

    @Override
    public List<ProcessDefinitionUserTask> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        List<ProcessDefinitionUserTask> tasks = new ArrayList<ProcessDefinitionUserTask>();
        ObjectCodec oc = jp.getCodec();
        JsonNode nodes = oc.readTree(jp);

        for (int i = 0; i < nodes.size(); i++) {
            ProcessDefinitionUserTask task = new ProcessDefinitionUserTask(nodes.get(i).get("taskName").asText(), nodes.get(i).get("taskDocumentation").asText());
            tasks.add(task);
        }

        return tasks;
    }
}
