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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.activiti.engine.ActivitiException;
import org.junit.jupiter.api.Test;

class TimerChangeProcessDefinitionSuspensionStateJobHandlerTest {

    @Test
    void createJobHandlerConfiguration_shouldProduceGoldenJson() {
        assertThat(TimerChangeProcessDefinitionSuspensionStateJobHandler.createJobHandlerConfiguration(true)).isEqualTo(
            "{\"includeProcessInstances\":true}"
        );
        assertThat(
            TimerChangeProcessDefinitionSuspensionStateJobHandler.createJobHandlerConfiguration(false)
        ).isEqualTo("{\"includeProcessInstances\":false}");
    }

    @Test
    void getIncludeProcessInstances_shouldReadPersistedBooleanValues() {
        assertThat(
            TimerChangeProcessDefinitionSuspensionStateJobHandler.getIncludeProcessInstances(
                "{\"includeProcessInstances\":true}"
            )
        ).isTrue();
        assertThat(
            TimerChangeProcessDefinitionSuspensionStateJobHandler.getIncludeProcessInstances(
                "{\"includeProcessInstances\":false}"
            )
        ).isFalse();
    }

    @Test
    void getIncludeProcessInstances_shouldPreservePersistedStringBooleanCompatibility() {
        assertThat(
            TimerChangeProcessDefinitionSuspensionStateJobHandler.getIncludeProcessInstances(
                "{\"includeProcessInstances\":\"TRUE\"}"
            )
        ).isTrue();
        assertThat(
            TimerChangeProcessDefinitionSuspensionStateJobHandler.getIncludeProcessInstances(
                "{\"includeProcessInstances\":\"false\"}"
            )
        ).isFalse();
    }

    @Test
    void getIncludeProcessInstances_withMissingValue_shouldFailWithContext() {
        assertInvalidConfiguration("{}");
    }

    @Test
    void getIncludeProcessInstances_withWrongValueType_shouldFailWithContext() {
        assertInvalidConfiguration("{\"includeProcessInstances\":1}");
    }

    @Test
    void getIncludeProcessInstances_withMalformedOrNonObjectJson_shouldFailWithContext() {
        assertInvalidConfiguration("not-json");
        assertInvalidConfiguration("[]");
        assertInvalidConfiguration("null");
    }

    private static void assertInvalidConfiguration(String configuration) {
        assertThatThrownBy(() ->
            TimerChangeProcessDefinitionSuspensionStateJobHandler.getIncludeProcessInstances(configuration)
        )
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("includeProcessInstances")
            .hasMessageContaining(configuration);
    }
}
