package org.activiti.api.runtime.model.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.activiti.api.runtime.model.impl.ProcessVariablesMap.ProcessVariableValue;
import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ProcessVariablesMapSerializer extends StdSerializer<ProcessVariablesMap<String, Object>> {

    private static final long serialVersionUID = 1L;

    private final ConversionService conversionService;

    public ProcessVariablesMapSerializer(ConversionService conversionService) {
        super(ProcessVariablesMap.class, true);

        this.conversionService = conversionService;
    }

    @Override
    public void serialize(ProcessVariablesMap<String, Object> processVariablesMap,
                          JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {

        HashMap<String, ProcessVariableValue> map = new HashMap<>();
        for (Map.Entry<String, Object> entry: processVariablesMap.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if(value != null) {
                String entryType = entry.getValue()
                                        .getClass()
                                        .getName();

                String entryValue = conversionService.convert(entry.getValue(),
                                                              String.class);

                ProcessVariableValue variableValue = new ProcessVariableValue(entryType,
                                                                              entryValue);
                map.put(name,
                        variableValue);

            } else {
                map.put(name,
                        null);
            }

        }

        gen.writeObject(map);
    }
}