package org.activiti.services.core.model;

import java.io.IOException;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ProcessDefinitionServiceTask extends JsonDeserializer<HashSet<ProcessDefinitionServiceTask>> {

    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("taskImplementation")
    private String taskImplementation;

    public ProcessDefinitionServiceTask() {
    }

    @JsonCreator
    public ProcessDefinitionServiceTask(String name, String implementation) {
        taskName = name;
        taskImplementation = implementation;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskImplementation() {
        return taskImplementation;
    }

    @Override
    public HashSet<ProcessDefinitionServiceTask> deserialize(JsonParser jp, DeserializationContext ctxt)
                                                                                                      throws IOException,
                                                                                                      JsonProcessingException {

        HashSet<ProcessDefinitionServiceTask> tasks = new HashSet<ProcessDefinitionServiceTask>();
        ObjectCodec oc = jp.getCodec();
        JsonNode nodes = oc.readTree(jp);

        for (int i = 0; i < nodes.size(); i++) {
            ProcessDefinitionServiceTask task = new ProcessDefinitionServiceTask(nodes.get(i).get("taskName").asText(),
                                                                                 nodes.get(i)
                                                                                      .get("taskImplementation")
                                                                                      .asText());
            tasks.add(task);
        }

        return tasks;
    }

}
