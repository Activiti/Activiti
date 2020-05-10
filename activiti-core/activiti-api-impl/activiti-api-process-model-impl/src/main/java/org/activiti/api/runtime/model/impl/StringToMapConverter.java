package org.activiti.api.runtime.model.impl;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StringToMapConverter implements Converter<String, Map<String, Object>> {
    private final ObjectMapper objectMapper;

    public StringToMapConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> convert(String source) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);

        try {
            return objectMapper.readValue(source, javaType);
        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }
}
