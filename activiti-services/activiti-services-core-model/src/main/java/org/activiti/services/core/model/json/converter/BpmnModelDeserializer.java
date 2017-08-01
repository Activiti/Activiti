package org.activiti.services.core.model.json.converter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;

public class BpmnModelDeserializer extends JsonDeserializer<BpmnModel> {

    @Override
    public BpmnModel deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException,
                                                                                        JsonProcessingException {
        BpmnJsonConverter converter = new BpmnJsonConverter();
        JsonNode json = jsonParser.getCodec().readTree(jsonParser);
        return converter.convertToBpmnModel(json);
    }
}
