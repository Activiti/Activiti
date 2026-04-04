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

class TimerEventHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createConfiguration_withAllFields_shouldProduceValidJson() throws Exception {
        String config = TimerEventHandler.createConfiguration("act1", "2026-01-01", "custom-cal");
        JsonNode node = objectMapper.readTree(config);
        assertThat(node.get("activityId").asText()).isEqualTo("act1");
        assertThat(node.get("timerEndDate").asText()).isEqualTo("2026-01-01");
        assertThat(node.get("calendarName").asText()).isEqualTo("custom-cal");
    }

    @Test
    void createConfiguration_withNullEndDateAndCalendar_shouldOmitThem() throws Exception {
        String config = TimerEventHandler.createConfiguration("act1", null, null);
        JsonNode node = objectMapper.readTree(config);
        assertThat(node.get("activityId").asText()).isEqualTo("act1");
        assertThat(node.has("timerEndDate")).isFalse();
        assertThat(node.has("calendarName")).isFalse();
    }

    @Test
    void getActivityIdFromConfiguration_withJsonInput_shouldReturnActivityId() {
        String config = TimerEventHandler.createConfiguration("act1", null, null);
        assertThat(TimerEventHandler.getActivityIdFromConfiguration(config)).isEqualTo("act1");
    }

    @Test
    void getActivityIdFromConfiguration_withPlainText_shouldReturnItAsIs() {
        assertThat(TimerEventHandler.getActivityIdFromConfiguration("plainActivityId"))
            .isEqualTo("plainActivityId");
    }

    @Test
    void setActivityIdToConfiguration_withJsonInput_shouldUpdateActivityId() throws Exception {
        String config = TimerEventHandler.createConfiguration("act1", "2026-01-01", null);
        String updated = TimerEventHandler.setActivityIdToConfiguration(config, "act2");
        JsonNode node = objectMapper.readTree(updated);
        assertThat(node.get("activityId").asText()).isEqualTo("act2");
        assertThat(node.get("timerEndDate").asText()).isEqualTo("2026-01-01");
    }

    @Test
    void setActivityIdToConfiguration_withPlainText_shouldReturnItAsIs() {
        assertThat(TimerEventHandler.setActivityIdToConfiguration("plainText", "act2"))
            .isEqualTo("plainText");
    }

    @Test
    void getEndDateFromConfiguration_withEndDate_shouldReturnIt() {
        String config = TimerEventHandler.createConfiguration("act1", "2026-01-01", null);
        assertThat(TimerEventHandler.getEndDateFromConfiguration(config)).isEqualTo("2026-01-01");
    }

    @Test
    void getEndDateFromConfiguration_withoutEndDate_shouldReturnNull() {
        String config = TimerEventHandler.createConfiguration("act1", null, null);
        assertThat(TimerEventHandler.getEndDateFromConfiguration(config)).isNull();
    }

    @Test
    void getEndDateFromConfiguration_withPlainText_shouldReturnNull() {
        assertThat(TimerEventHandler.getEndDateFromConfiguration("plainText")).isNull();
    }

    @Test
    void geCalendarNameFromConfiguration_withCalendar_shouldReturnIt() {
        String config = TimerEventHandler.createConfiguration("act1", null, "custom-cal");
        assertThat(TimerEventHandler.geCalendarNameFromConfiguration(config)).isEqualTo("custom-cal");
    }

    @Test
    void geCalendarNameFromConfiguration_withoutCalendar_shouldReturnEmpty() {
        String config = TimerEventHandler.createConfiguration("act1", null, null);
        assertThat(TimerEventHandler.geCalendarNameFromConfiguration(config)).isEmpty();
    }

    @Test
    void geCalendarNameFromConfiguration_withPlainText_shouldReturnEmpty() {
        assertThat(TimerEventHandler.geCalendarNameFromConfiguration("plainText")).isEmpty();
    }

    @Test
    void setEndDateToConfiguration_withJsonInput_shouldAddEndDate() throws Exception {
        String config = TimerEventHandler.createConfiguration("act1", null, null);
        String updated = TimerEventHandler.setEndDateToConfiguration(config, "2026-06-01");
        JsonNode node = objectMapper.readTree(updated);
        assertThat(node.get("activityId").asText()).isEqualTo("act1");
        assertThat(node.get("timerEndDate").asText()).isEqualTo("2026-06-01");
    }

    @Test
    void setEndDateToConfiguration_withPlainText_shouldCreateJsonWithActivityIdAndEndDate()
        throws Exception {
        String updated = TimerEventHandler.setEndDateToConfiguration("plainActivityId", "2026-06-01");
        JsonNode node = objectMapper.readTree(updated);
        assertThat(node.get("activityId").asText()).isEqualTo("plainActivityId");
        assertThat(node.get("timerEndDate").asText()).isEqualTo("2026-06-01");
    }

    @Test
    void setEndDateToConfiguration_withNullEndDate_shouldNotAddEndDate() throws Exception {
        String config = TimerEventHandler.createConfiguration("act1", null, null);
        String updated = TimerEventHandler.setEndDateToConfiguration(config, null);
        JsonNode node = objectMapper.readTree(updated);
        assertThat(node.get("activityId").asText()).isEqualTo("act1");
        assertThat(node.has("timerEndDate")).isFalse();
    }

    @Test
    void roundTrip_createAndReadBack_shouldPreserveAllValues() {
        String config = TimerEventHandler.createConfiguration("myActivity", "2026-12-31", "businessCal");
        assertThat(TimerEventHandler.getActivityIdFromConfiguration(config)).isEqualTo("myActivity");
        assertThat(TimerEventHandler.getEndDateFromConfiguration(config)).isEqualTo("2026-12-31");
        assertThat(TimerEventHandler.geCalendarNameFromConfiguration(config)).isEqualTo("businessCal");
    }
}
