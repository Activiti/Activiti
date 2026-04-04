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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TimerEventHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String PROPERTYNAME_TIMER_ACTIVITY_ID = "activityId";
    public static final String PROPERTYNAME_END_DATE_EXPRESSION = "timerEndDate";
    public static final String PROPERTYNAME_CALENDAR_NAME_EXPRESSION = "calendarName";

    public static String createConfiguration(String id, String endDate, String calendarName) {
        ObjectNode cfgJson = objectMapper.createObjectNode();
        cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, id);
        if (endDate != null) {
            cfgJson.put(PROPERTYNAME_END_DATE_EXPRESSION, endDate);
        }
        if (calendarName != null) {
            cfgJson.put(PROPERTYNAME_CALENDAR_NAME_EXPRESSION, calendarName);
        }
        return cfgJson.toString();
    }

    public static String setActivityIdToConfiguration(String jobHandlerConfiguration, String activityId) {
        try {
            ObjectNode cfgJson = (ObjectNode) objectMapper.readTree(jobHandlerConfiguration);
            cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, activityId);
            return cfgJson.toString();
        } catch (JsonProcessingException ex) {
            return jobHandlerConfiguration;
        }
    }

    public static String getActivityIdFromConfiguration(String jobHandlerConfiguration) {
        try {
            return objectMapper.readTree(jobHandlerConfiguration).get(PROPERTYNAME_TIMER_ACTIVITY_ID).asText();
        } catch (JsonProcessingException | NullPointerException ex) {
            return jobHandlerConfiguration;
        }
    }

    public static String geCalendarNameFromConfiguration(String jobHandlerConfiguration) {
        try {
            return objectMapper.readTree(jobHandlerConfiguration).get(PROPERTYNAME_CALENDAR_NAME_EXPRESSION).asText();
        } catch (JsonProcessingException | NullPointerException ex) {
            return "";
        }
    }

    public static String setEndDateToConfiguration(String jobHandlerConfiguration, String endDate) {
        ObjectNode cfgJson;
        try {
            cfgJson = (ObjectNode) objectMapper.readTree(jobHandlerConfiguration);
        } catch (JsonProcessingException ex) {
            cfgJson = objectMapper.createObjectNode();
            cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, jobHandlerConfiguration);
        }
        if (endDate != null) {
            cfgJson.put(PROPERTYNAME_END_DATE_EXPRESSION, endDate);
        }
        return cfgJson.toString();
    }

    public static String getEndDateFromConfiguration(String jobHandlerConfiguration) {
        try {
            return objectMapper.readTree(jobHandlerConfiguration).get(PROPERTYNAME_END_DATE_EXPRESSION).asText();
        } catch (JsonProcessingException | NullPointerException ex) {
            return null;
        }
    }
}
