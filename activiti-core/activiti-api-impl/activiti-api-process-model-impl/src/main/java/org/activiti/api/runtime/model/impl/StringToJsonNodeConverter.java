package org.activiti.api.runtime.model.impl;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ProcessVariableTypeConverter
public class StringToJsonNodeConverter implements Converter<String, JsonNode> {
    private final ObjectMapper objectMapper;

    public StringToJsonNodeConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode convert(String source) {

        try {
            return objectMapper.readValue(source, JsonNode.class);
        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }
}
