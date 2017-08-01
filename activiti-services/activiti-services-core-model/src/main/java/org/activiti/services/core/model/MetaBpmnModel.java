package org.activiti.services.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.services.core.model.json.converter.BpmnModelDeserializer;
import org.activiti.services.core.model.json.converter.BpmnModelSerializer;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaBpmnModel {

    private String id;

    @JsonSerialize(using = BpmnModelSerializer.class)
    @JsonDeserialize(using = BpmnModelDeserializer.class)
    private BpmnModel content;

    public MetaBpmnModel() {
    }

    public MetaBpmnModel(String id,
                         BpmnModel content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public BpmnModel getContent() {
        return content;
    }
}
