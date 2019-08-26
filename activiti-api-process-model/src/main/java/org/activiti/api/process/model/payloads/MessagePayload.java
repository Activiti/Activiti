package org.activiti.api.process.model.payloads;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class MessagePayload implements Payload {

    private final String id;
    private String name;
    private String correlationKey; 
    private String businessKey;
    private Map<String, Object> variables = new LinkedHashMap<>();

    public MessagePayload() {
        this.id = UUID.randomUUID().toString();
    }

    public MessagePayload(String name, 
                          String correlationKey,
                          String businessKey,
                          Map<String, Object> variables) {
        this();

        Objects.requireNonNull(name, "name must not be null");
        
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public String getCorrelationKey() {
        return correlationKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(businessKey, correlationKey, id, name, variables);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessagePayload other = (MessagePayload) obj;
        return Objects.equals(businessKey, other.businessKey) 
                && Objects.equals(correlationKey, other.correlationKey) 
                && Objects.equals(id, other.id) 
                && Objects.equals(name, other.name) 
                && Objects.equals(variables, other.variables);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessagePayload [id=");
        builder.append(id);
        builder.append(", name=");
        builder.append(name);
        builder.append(", correlationKey=");
        builder.append(correlationKey);
        builder.append(", businessKey=");
        builder.append(businessKey);
        builder.append(", variables=");
        builder.append(variables);
        builder.append("]");
        return builder.toString();
    }

}
