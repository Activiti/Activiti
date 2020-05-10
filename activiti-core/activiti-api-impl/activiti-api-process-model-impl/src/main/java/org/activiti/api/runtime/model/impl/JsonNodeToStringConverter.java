package org.activiti.api.runtime.model.impl;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ProcessVariableTypeConverter
public class JsonNodeToStringConverter implements Converter<JsonNode, String> {
    private final ObjectMapper objectMapper;

    public JsonNodeToStringConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convert(JsonNode source) {

        try {
            return objectMapper.writeValueAsString(source);
        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }
}
