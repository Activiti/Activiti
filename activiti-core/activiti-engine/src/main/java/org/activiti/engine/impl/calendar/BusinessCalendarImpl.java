/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.calendar;

import java.util.Date;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.runtime.ClockReader;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This class implements business calendar based on internal clock
 */
@Internal
public abstract class BusinessCalendarImpl implements BusinessCalendar {

  protected ClockReader clockReader;

  public BusinessCalendarImpl(ClockReader clockReader) {
    this.clockReader = clockReader;
  }

  @Override
  public Date resolveDuedate(String duedateDescription) {
    return resolveDuedate(duedateDescription, -1);
  }

  public abstract Date resolveDuedate(String duedateDescription, int maxIterations);

  @Override
  public Boolean validateDuedate(String duedateDescription, int maxIterations, Date endDate, Date newTimer) {
    return endDate == null || endDate.after(newTimer) || endDate.equals(newTimer);
  }

  @Override
  public Date resolveEndDate(String endDateString) {
    return ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.forTimeZone(clockReader.getCurrentTimeZone())).parseDateTime(endDateString).toCalendar(null).getTime();
  }

}
