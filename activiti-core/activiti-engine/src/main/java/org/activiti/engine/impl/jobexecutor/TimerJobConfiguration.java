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

import org.activiti.engine.ActivitiException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Owns the persisted JSON format used by timer jobs.
 */
final class TimerJobConfiguration {

    static final String ACTIVITY_ID = "activityId";
    static final String TIMER_END_DATE = "timerEndDate";
    static final String CALENDAR_NAME = "calendarName";

    private static final String INCLUDE_PROCESS_INSTANCES = "includeProcessInstances";
    private static final JsonMapper JSON_MAPPER = JsonMapper.shared();

    private TimerJobConfiguration() {}

    static String createTimerEvent(String activityId, String endDate, String calendarName) {
        ObjectNode configuration = JSON_MAPPER.createObjectNode();
        putOrRemove(configuration, ACTIVITY_ID, activityId);
        if (endDate != null) {
            configuration.put(TIMER_END_DATE, endDate);
        }
        if (calendarName != null) {
            configuration.put(CALENDAR_NAME, calendarName);
        }
        return configuration.toString();
    }

    static String setActivityId(String jobHandlerConfiguration, String activityId) {
        ObjectNode configuration = readObjectOrNull(jobHandlerConfiguration);
        if (configuration == null) {
            return jobHandlerConfiguration;
        }

        putOrRemove(configuration, ACTIVITY_ID, activityId);
        return configuration.toString();
    }

    static String getActivityId(String jobHandlerConfiguration) {
        String activityId = getValueAsString(readObjectOrNull(jobHandlerConfiguration), ACTIVITY_ID);
        return activityId != null ? activityId : jobHandlerConfiguration;
    }

    static String getCalendarName(String jobHandlerConfiguration) {
        String calendarName = getValueAsString(readObjectOrNull(jobHandlerConfiguration), CALENDAR_NAME);
        return calendarName != null ? calendarName : "";
    }

    static String setEndDate(String jobHandlerConfiguration, String endDate) {
        ObjectNode configuration = readObjectOrNull(jobHandlerConfiguration);
        if (configuration == null) {
            configuration = JSON_MAPPER.createObjectNode();
            putOrRemove(configuration, ACTIVITY_ID, jobHandlerConfiguration);
        }
        if (endDate != null) {
            configuration.put(TIMER_END_DATE, endDate);
        }
        return configuration.toString();
    }

    static String getEndDate(String jobHandlerConfiguration) {
        return getValueAsString(readObjectOrNull(jobHandlerConfiguration), TIMER_END_DATE);
    }

    static String createProcessDefinitionStateChange(boolean includeProcessInstances) {
        ObjectNode configuration = JSON_MAPPER.createObjectNode();
        configuration.put(INCLUDE_PROCESS_INSTANCES, includeProcessInstances);
        return configuration.toString();
    }

    static boolean getIncludeProcessInstances(String jobHandlerConfiguration) {
        ObjectNode configuration;
        try {
            configuration = readRequiredObject(jobHandlerConfiguration);
        } catch (JacksonException exception) {
            throw invalidProcessDefinitionStateChangeConfiguration(jobHandlerConfiguration, exception);
        }

        JsonNode includeProcessInstances = configuration.get(INCLUDE_PROCESS_INSTANCES);
        if (includeProcessInstances != null && includeProcessInstances.isBoolean()) {
            return includeProcessInstances.booleanValue();
        }
        if (includeProcessInstances != null && includeProcessInstances.isTextual()) {
            String value = includeProcessInstances.textValue();
            if ("true".equalsIgnoreCase(value)) {
                return true;
            }
            if ("false".equalsIgnoreCase(value)) {
                return false;
            }
        }

        throw invalidProcessDefinitionStateChangeConfiguration(jobHandlerConfiguration, null);
    }

    private static ObjectNode readObjectOrNull(String jobHandlerConfiguration) {
        if (jobHandlerConfiguration == null) {
            return null;
        }

        try {
            JsonNode configuration = JSON_MAPPER.readTree(jobHandlerConfiguration);
            return configuration != null && configuration.isObject() ? (ObjectNode) configuration : null;
        } catch (JacksonException _) {
            return null;
        }
    }

    private static ObjectNode readRequiredObject(String jobHandlerConfiguration) {
        if (jobHandlerConfiguration == null) {
            throw invalidProcessDefinitionStateChangeConfiguration(null, null);
        }

        JsonNode configuration = JSON_MAPPER.readTree(jobHandlerConfiguration);
        if (configuration == null || !configuration.isObject()) {
            throw invalidProcessDefinitionStateChangeConfiguration(jobHandlerConfiguration, null);
        }
        return (ObjectNode) configuration;
    }

    private static String getValueAsString(ObjectNode configuration, String propertyName) {
        if (configuration == null) {
            return null;
        }

        JsonNode value = configuration.get(propertyName);
        if (value == null) {
            return null;
        }
        return value.isTextual() ? value.textValue() : value.toString();
    }

    private static void putOrRemove(ObjectNode configuration, String propertyName, String value) {
        if (value == null) {
            configuration.remove(propertyName);
        } else {
            configuration.put(propertyName, value);
        }
    }

    private static ActivitiException invalidProcessDefinitionStateChangeConfiguration(
        String jobHandlerConfiguration,
        Throwable cause
    ) {
        String message =
            "Invalid timer job handler configuration: expected '" +
            INCLUDE_PROCESS_INSTANCES +
            "' to be a boolean in a JSON object: " +
            jobHandlerConfiguration;
        return cause == null ? new ActivitiException(message) : new ActivitiException(message, cause);
    }
}
