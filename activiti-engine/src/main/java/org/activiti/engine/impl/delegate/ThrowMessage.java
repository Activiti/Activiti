package org.activiti.engine.impl.delegate;

import java.util.Optional;

public class ThrowMessage {
    private final String name;
    private Optional<Object> payload;

    private ThrowMessage(Builder builder) {
        this.name = builder.name;
        this.payload = builder.payload;
    }

    public ThrowMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public Optional<Object> getPayload() {
        return payload;
    }

    public void setPayload(Optional<Object> payload) {
        this.payload = payload;
    }

    /**
     * Creates builder to build {@link ThrowMessage}.
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }


    /**
     * Creates a builder to build {@link ThrowMessage} and initialize it with the given object.
     * @param throwMessage to initialize the builder with
     * @return created builder
     */
    public static Builder builderFrom(ThrowMessage throwMessage) {
        return new Builder(throwMessage);
    }


    /**
     * Builder to build {@link ThrowMessage}.
     */
    public static final class Builder {

        private String name;
        private Optional<Object> payload;

        private Builder() {
        }

        private Builder(ThrowMessage throwMessage) {
            this.name = throwMessage.name;
            this.payload = throwMessage.payload;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder payload(Optional<Object> payload) {
            this.payload = payload;
            return this;
        }

        public ThrowMessage build() {
            return new ThrowMessage(this);
        }
    }

}
