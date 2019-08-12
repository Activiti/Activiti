package org.activiti.engine.impl.delegate;

import java.util.Map;
import java.util.Optional;

public class ThrowMessage {
    private final String name;
    private Optional<Map<String, Object>> payload;
    private Optional<String> businessKey;

    private ThrowMessage(Builder builder) {
        this.name = builder.name;
        this.payload = builder.payload;
        this.businessKey = builder.businessKey;
    }

    public ThrowMessage(String name) {
        this.name = name;
    }

    /**
     * Creates a builder to build {@link ThrowMessage} and initialize it with the given object.
     * @param throwMessage to initialize the builder with
     * @return created builder
     */
    public static Builder builderFrom(ThrowMessage throwMessage) {
        return new Builder(throwMessage);
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public String getName() {
        return name;
    }
    
    public Optional<Map<String, Object>> getPayload() {
        return payload;
    }

    public void setPayload(Optional<Map<String, Object>> payload) {
        this.payload = payload;
    }

    public Optional<String> getBusinessKey() {
        return businessKey;
    }

    
    public void setBusinessKey(Optional<String> businessKey) {
        this.businessKey = businessKey;
    }

    /**
     * Builder to build {@link ThrowMessage}.
     */
    public static final class Builder {

        private String name;
        private Optional<Map<String, Object>> payload = Optional.empty();
        private Optional<String> businessKey = Optional.empty();

        public Builder() {
        }

        private Builder(ThrowMessage throwMessage) {
            this.name = throwMessage.name;
            this.payload = throwMessage.payload;
            this.businessKey = throwMessage.businessKey;
        }

        /**
        * Builder method for name parameter.
        * @param name field to set
        * @return builder
        */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
        * Builder method for payload parameter.
        * @param payload field to set
        * @return builder
        */
        public Builder payload(Optional<Map<String, Object>> payload) {
            this.payload = payload;
            return this;
        }

        /**
        * Builder method for businessKey parameter.
        * @param businessKey field to set
        * @return builder
        */
        public Builder businessKey(Optional<String> businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        /**
        * Builder method of the builder.
        * @return built class
        */
        public ThrowMessage build() {
            return new ThrowMessage(this);
        }
    }
}
