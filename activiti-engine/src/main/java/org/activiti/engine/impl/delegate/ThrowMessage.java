/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.delegate;

import java.util.Map;
import java.util.Optional;

public class ThrowMessage {
    private String name;
    private Optional<Map<String, Object>> payload = Optional.empty();
    private Optional<String> businessKey = Optional.empty();
    private Optional<String> correlationKey = Optional.empty();

    private ThrowMessage(ThrowMessagBuilder builder) {
        this.name = builder.name;
        this.payload = builder.payload;
        this.businessKey = builder.businessKey;
        this.correlationKey = builder.correlationKey;
    }

    ThrowMessage() {
    }

    public ThrowMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public Optional<Map<String, Object>> getPayload() {
        return payload;
    }

    public Optional<String> getBusinessKey() {
        return businessKey;
    }

    public Optional<String> getCorrelationKey() {
        return correlationKey;
    }

    /**
     * Creates builder to build {@link ThrowMessage}.
     * @return created builder
     */
    public static INameStage builder() {
        return new ThrowMessagBuilder();
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface INameStage {

        /**
        * Builder method for name parameter.
        * @param name field to set
        * @return builder
        */
        public IBuildStage name(String name);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IBuildStage {

        /**
        * Builder method for payload parameter.
        * @param payload field to set
        * @return builder
        */
        public IBuildStage payload(Optional<Map<String, Object>> payload);

        /**
        * Builder method for businessKey parameter.
        * @param businessKey field to set
        * @return builder
        */
        public IBuildStage businessKey(Optional<String> businessKey);

        /**
        * Builder method for correlationKey parameter.
        * @param correlationKey field to set
        * @return builder
        */
        public IBuildStage correlationKey(Optional<String> correlationKey);

        /**
        * Builder method of the builder.
        * @return built class
        */
        public ThrowMessage build();
    }

    /**
     * Builder to build {@link ThrowMessage}.
     */
    public static final class ThrowMessagBuilder implements INameStage, IBuildStage {

        private String name;
        private Optional<Map<String, Object>> payload = Optional.empty();
        private Optional<String> businessKey = Optional.empty();
        private Optional<String> correlationKey = Optional.empty();

        private ThrowMessagBuilder() {
        }

        @Override
        public IBuildStage name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public IBuildStage payload(Optional<Map<String, Object>> payload) {
            this.payload = payload;
            return this;
        }

        @Override
        public IBuildStage businessKey(Optional<String> businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        @Override
        public IBuildStage correlationKey(Optional<String> correlationKey) {
            this.correlationKey = correlationKey;
            return this;
        }

        @Override
        public ThrowMessage build() {
            return new ThrowMessage(this);
        }
    }
}
