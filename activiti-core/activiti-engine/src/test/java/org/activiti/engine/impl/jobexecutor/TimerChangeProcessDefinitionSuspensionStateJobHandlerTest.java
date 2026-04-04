/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class TimerChangeProcessDefinitionSuspensionStateJobHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createJobHandlerConfiguration_withTrue_shouldProduceValidJson() throws Exception {
        String config = TimerChangeProcessDefinitionSuspensionStateJobHandler.createJobHandlerConfiguration(true);
        JsonNode node = objectMapper.readTree(config);
        assertThat(node.get("includeProcessInstances").asBoolean()).isTrue();
    }

    @Test
    void createJobHandlerConfiguration_withFalse_shouldProduceValidJson() throws Exception {
        String config = TimerChangeProcessDefinitionSuspensionStateJobHandler.createJobHandlerConfiguration(false);
        JsonNode node = objectMapper.readTree(config);
        assertThat(node.get("includeProcessInstances").asBoolean()).isFalse();
    }

    @Test
    void roundTrip_createAndParseThenGetIncludeProcessInstances_true() throws Exception {
        String config = TimerChangeProcessDefinitionSuspensionStateJobHandler.createJobHandlerConfiguration(true);
        JsonNode node = objectMapper.readTree(config);
        assertThat(TimerChangeProcessDefinitionSuspensionStateJobHandler.getIncludeProcessInstances(node)).isTrue();
    }

    @Test
    void roundTrip_createAndParseThenGetIncludeProcessInstances_false() throws Exception {
        String config = TimerChangeProcessDefinitionSuspensionStateJobHandler.createJobHandlerConfiguration(false);
        JsonNode node = objectMapper.readTree(config);
        assertThat(TimerChangeProcessDefinitionSuspensionStateJobHandler.getIncludeProcessInstances(node)).isFalse();
    }
}
