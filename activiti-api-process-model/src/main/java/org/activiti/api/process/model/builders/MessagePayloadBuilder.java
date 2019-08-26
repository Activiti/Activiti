package org.activiti.api.process.model.builders;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.activiti.api.process.model.payloads.MessagePayload;

public class MessagePayloadBuilder {

    private String name;
    private String correlationKey;
    private String businessKey;
    private Map<String, Object> variables;

    public static MessagePayloadBuilder from(MessagePayload messagePayload) {
        Objects.requireNonNull(messagePayload, "messagePayload must not be null");
        
        return new MessagePayloadBuilder().withName(messagePayload.getName())
                                          .withCorrelationKey(messagePayload.getCorrelationKey())
                                          .withBusinessKey(messagePayload.getBusinessKey())
                                          .withVariables(messagePayload.getVariables());
    }
    
    public static MessagePayloadBuilder message(String name) {
        return new MessagePayloadBuilder().withName(name);
    }
    
    public MessagePayloadBuilder withName(String name) {
        Objects.requireNonNull(name, "name must not be null");

        this.name = name;
        
        return this;
    }

    public MessagePayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        
        return this;
    }

    public MessagePayloadBuilder withVariable(String name,
                                             Object value) {
        if (this.variables == null) {
            this.variables = new LinkedHashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public MessagePayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        
        return this;
    }

    public MessagePayloadBuilder withCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
        
        return this;
    }
    
    public MessagePayload build() {
        return new MessagePayload(name,
                                  correlationKey,
                                  businessKey,
                                  this.variables);
    }
}
