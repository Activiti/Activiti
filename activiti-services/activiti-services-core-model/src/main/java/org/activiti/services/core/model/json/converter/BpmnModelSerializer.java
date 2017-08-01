package org.activiti.services.core.model.json.converter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;

public class BpmnModelSerializer extends JsonSerializer<BpmnModel> {

    @Override
    public void serialize(BpmnModel model, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                                                                                                               throws IOException,
                                                                                                               JsonProcessingException {
        BpmnJsonConverter converter = new BpmnJsonConverter();
        jsonGenerator.writeTree(converter.convertToJson(model));
    }

}
