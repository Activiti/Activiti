package org.activiti.api.runtime.model.impl;

import java.io.IOException;

import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

@ProcessVariableTypeConverter
public class ProcessVariablesMapDeserializer extends JsonDeserializer<ProcessVariablesMap<String, Object>> {

    private final ConversionService conversionService;

    public ProcessVariablesMapDeserializer(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public ProcessVariablesMap<String, Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
                                                                                                              JsonProcessingException {
        ProcessVariablesMap<String, Object> map = new ProcessVariablesMap<>();

        JsonNode node = jp.getCodec().readTree(jp);

        node.fields().forEachRemaining(entry -> {
            String name = entry.getKey();
            JsonNode entryValue = entry.getValue();

            if(!entryValue.isNull()) {
                String type = entryValue.get("type").textValue();
                String value = entryValue.get("value").asText();

                try {
                    Class<?> clazz = Class.forName(type);
                    Object result = conversionService.convert(value, clazz);

                    map.put(name, result);
                } catch (ClassNotFoundException e) {
                    map.put(name, null);
                }
            } else {
                map.put(name, null);
            }
        });

        return map;
    }
}