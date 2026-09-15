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

public class TimerEventHandler {

    public static final String PROPERTYNAME_TIMER_ACTIVITY_ID = TimerJobConfiguration.ACTIVITY_ID;
    public static final String PROPERTYNAME_END_DATE_EXPRESSION = TimerJobConfiguration.TIMER_END_DATE;
    public static final String PROPERTYNAME_CALENDAR_NAME_EXPRESSION = TimerJobConfiguration.CALENDAR_NAME;

    public static String createConfiguration(String id, String endDate, String calendarName) {
        return TimerJobConfiguration.createTimerEvent(id, endDate, calendarName);
    }

    public static String setActivityIdToConfiguration(String jobHandlerConfiguration, String activityId) {
        return TimerJobConfiguration.setActivityId(jobHandlerConfiguration, activityId);
    }

    public static String getActivityIdFromConfiguration(String jobHandlerConfiguration) {
        return TimerJobConfiguration.getActivityId(jobHandlerConfiguration);
    }

    public static String geCalendarNameFromConfiguration(String jobHandlerConfiguration) {
        return TimerJobConfiguration.getCalendarName(jobHandlerConfiguration);
    }

    public static String setEndDateToConfiguration(String jobHandlerConfiguration, String endDate) {
        return TimerJobConfiguration.setEndDate(jobHandlerConfiguration, endDate);
    }

    public static String getEndDateFromConfiguration(String jobHandlerConfiguration) {
        return TimerJobConfiguration.getEndDate(jobHandlerConfiguration);
    }
}
