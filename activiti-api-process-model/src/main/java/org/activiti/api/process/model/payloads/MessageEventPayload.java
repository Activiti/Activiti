package org.activiti.api.process.model.payloads;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class MessageEventPayload implements Payload {

    private String id;
    private String name;
    private String correlationKey;
    private String businessKey;
    private Map<String, Object> variables;

    public MessageEventPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public MessageEventPayload(String name,
                               String correlationKey,
                               String businessKey,
                               Map<String, Object> variables) {
        this();
        this.name = name;
        this.businessKey = businessKey;
        this.correlationKey = correlationKey;
        this.variables = variables;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageEventPayload [id=");
        builder.append(id);
        builder.append(", messageName=");
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
        MessageEventPayload other = (MessageEventPayload) obj;
        return Objects.equals(businessKey, other.businessKey) 
                && Objects.equals(correlationKey, other.correlationKey) 
                && Objects.equals(id, other.id) 
                && Objects.equals(name, other.name) 
                && Objects.equals(variables, other.variables);
    }

}
