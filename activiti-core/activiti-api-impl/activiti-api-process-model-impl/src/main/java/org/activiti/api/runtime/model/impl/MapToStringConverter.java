package org.activiti.api.runtime.model.impl;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapToStringConverter implements Converter<Map<String, Object>, String> {
    private final ObjectMapper objectMapper;

    public MapToStringConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convert(Map<String, Object> source) {

        try {
            return objectMapper.writeValueAsString(source);
        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }
}
