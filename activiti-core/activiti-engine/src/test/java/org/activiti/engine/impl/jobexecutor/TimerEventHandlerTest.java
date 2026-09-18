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

import org.junit.jupiter.api.Test;

class TimerEventHandlerTest {

    private static final String PERSISTED_JSON_CONFIGURATION =
        "{\"activityId\":\"act1\",\"calendarName\":\"custom-cal\",\"timerEndDate\":\"2026-01-01\"}";

    @Test
    void createConfiguration_withAllFields_shouldProduceGoldenJson() {
        assertThat(TimerEventHandler.createConfiguration("act1", "2026-01-01", "custom-cal")).isEqualTo(
            "{\"activityId\":\"act1\",\"timerEndDate\":\"2026-01-01\",\"calendarName\":\"custom-cal\"}"
        );
    }

    @Test
    void createConfiguration_withNullValues_shouldOmitThem() {
        assertThat(TimerEventHandler.createConfiguration("act1", null, null)).isEqualTo("{\"activityId\":\"act1\"}");
        assertThat(TimerEventHandler.createConfiguration(null, null, null)).isEqualTo("{}");
    }

    @Test
    void getters_withPersistedJson_shouldReadAllValues() {
        assertThat(TimerEventHandler.getActivityIdFromConfiguration(PERSISTED_JSON_CONFIGURATION)).isEqualTo("act1");
        assertThat(TimerEventHandler.getEndDateFromConfiguration(PERSISTED_JSON_CONFIGURATION)).isEqualTo("2026-01-01");
        assertThat(TimerEventHandler.geCalendarNameFromConfiguration(PERSISTED_JSON_CONFIGURATION)).isEqualTo(
            "custom-cal"
        );
    }

    @Test
    void getters_withLegacyPlainText_shouldPreserveFallbacks() {
        assertThat(TimerEventHandler.getActivityIdFromConfiguration("plainActivityId")).isEqualTo("plainActivityId");
        assertThat(TimerEventHandler.getEndDateFromConfiguration("plainActivityId")).isNull();
        assertThat(TimerEventHandler.geCalendarNameFromConfiguration("plainActivityId")).isEmpty();
    }

    @Test
    void getters_withMissingFields_shouldPreserveFallbacks() {
        String configuration = "{\"unrelated\":true}";

        assertThat(TimerEventHandler.getActivityIdFromConfiguration(configuration)).isEqualTo(configuration);
        assertThat(TimerEventHandler.getEndDateFromConfiguration(configuration)).isNull();
        assertThat(TimerEventHandler.geCalendarNameFromConfiguration(configuration)).isEmpty();
    }

    @Test
    void getters_withNonObjectJson_shouldPreserveFallbacks() {
        String configuration = "[\"act1\"]";

        assertThat(TimerEventHandler.getActivityIdFromConfiguration(configuration)).isEqualTo(configuration);
        assertThat(TimerEventHandler.getEndDateFromConfiguration(configuration)).isNull();
        assertThat(TimerEventHandler.geCalendarNameFromConfiguration(configuration)).isEmpty();
    }

    @Test
    void setActivityIdToConfiguration_withPersistedJson_shouldUpdateActivityIdAndPreserveOtherFields() {
        assertThat(TimerEventHandler.setActivityIdToConfiguration(PERSISTED_JSON_CONFIGURATION, "act2")).isEqualTo(
            "{\"activityId\":\"act2\",\"calendarName\":\"custom-cal\",\"timerEndDate\":\"2026-01-01\"}"
        );
    }

    @Test
    void setActivityIdToConfiguration_withNullActivityId_shouldRemoveActivityId() {
        assertThat(TimerEventHandler.setActivityIdToConfiguration(PERSISTED_JSON_CONFIGURATION, null)).isEqualTo(
            "{\"calendarName\":\"custom-cal\",\"timerEndDate\":\"2026-01-01\"}"
        );
    }

    @Test
    void setActivityIdToConfiguration_withLegacyPlainText_shouldReturnItAsIs() {
        assertThat(TimerEventHandler.setActivityIdToConfiguration("plainActivityId", "act2")).isEqualTo(
            "plainActivityId"
        );
    }

    @Test
    void setEndDateToConfiguration_withPersistedJson_shouldUpdateEndDateAndPreserveOtherFields() {
        assertThat(TimerEventHandler.setEndDateToConfiguration(PERSISTED_JSON_CONFIGURATION, "2026-06-01")).isEqualTo(
            "{\"activityId\":\"act1\",\"calendarName\":\"custom-cal\",\"timerEndDate\":\"2026-06-01\"}"
        );
    }

    @Test
    void setEndDateToConfiguration_withLegacyPlainText_shouldUpgradeItToJson() {
        assertThat(TimerEventHandler.setEndDateToConfiguration("plainActivityId", "2026-06-01")).isEqualTo(
            "{\"activityId\":\"plainActivityId\",\"timerEndDate\":\"2026-06-01\"}"
        );
    }

    @Test
    void setEndDateToConfiguration_withNullEndDate_shouldLeaveExistingEndDateUnchanged() {
        assertThat(TimerEventHandler.setEndDateToConfiguration(PERSISTED_JSON_CONFIGURATION, null)).isEqualTo(
            PERSISTED_JSON_CONFIGURATION
        );
    }
}
