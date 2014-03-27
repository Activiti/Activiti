/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.activiti.engine.impl.calendar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.util.TimeZoneUtil;
import org.activiti.engine.runtime.ClockReader;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * helper class for parsing ISO8601 duration format (also recurring) and
 * computing next timer date
 */
public class DurationHelper {

  private Calendar start;

  private Calendar end;

  private Duration period;

  private boolean isRepeat;

  private int times;

  private DatatypeFactory datatypeFactory;

  public Calendar getStart() {
    return start;
  }

  public Calendar getEnd() {
    return end;
  }

  public Duration getPeriod() {
    return period;
  }

  public boolean isRepeat() {
    return isRepeat;
  }

  public int getTimes() {
    return times;
  }

  protected ClockReader clockReader;

  public DurationHelper(String expressionS, ClockReader clockReader) throws Exception {
    this.clockReader = clockReader;
    List<String> expression = Arrays.asList(expressionS.split("/"));
    datatypeFactory = DatatypeFactory.newInstance();

    if (expression.size() > 3 || expression.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Cannot parse duration");
    }
    if (expression.get(0).startsWith("R")) {
      isRepeat = true;
      times = expression.get(0).length() == 1 ? Integer.MAX_VALUE : Integer.parseInt(expression.get(0).substring(1));
      expression = expression.subList(1, expression.size());
    }

    if (isDuration(expression.get(0))) {
      period = parsePeriod(expression.get(0));
      end = expression.size() == 1 ? null : parseDate(expression.get(1));
    } else {
      start = parseDate(expression.get(0));
      if (isDuration(expression.get(1))) {
        period = parsePeriod(expression.get(1));
      } else {
        end = parseDate(expression.get(1));
        period = datatypeFactory.newDuration(end.getTimeInMillis() - start.getTimeInMillis());
      }
    }
    if (start == null && end == null) {
      start = clockReader.getCurrentCalendar();
    }

  }

  public Calendar getCalendarAfter() {
    return getCalendarAfter(clockReader.getCurrentCalendar());
  }
  
  public Calendar getCalendarAfter(Calendar time) {
    if (isRepeat) {
      return getDateAfterRepeat(time);
    }
    // TODO: is this correct?
    if (end != null) {
      return end;
    }
    return add(start, period);
  }

  public Date getDateAfter() {
    Calendar date = getCalendarAfter();

    return date == null ? null : date.getTime();
  }

  private Calendar getDateAfterRepeat(Calendar date) {
    if (start != null) {
      Calendar cur = TimeZoneUtil.convertToTimeZone(start, date.getTimeZone());

      for (int i = 0; i < times && !cur.after(date); i++) {
        cur = add(cur, period);
      }

      return cur.before(date) ? null : TimeZoneUtil.convertToTimeZone(cur, clockReader.getCurrentTimeZone());
    }

    Calendar cur = add(TimeZoneUtil.convertToTimeZone(end, date.getTimeZone()), period.negate());

    Calendar next = TimeZoneUtil.convertToTimeZone(end, date.getTimeZone());

    for (int i = 0; i < times && cur.after(date); i++) {
      next = cur;
      cur = add(cur, period.negate());
    }

    return next.before(date) ? null : TimeZoneUtil.convertToTimeZone(next, clockReader.getCurrentTimeZone());
  }

  private Calendar add(Calendar date, Duration duration) {
    Calendar calendar = (Calendar) date.clone();

    // duration.addTo does not account for daylight saving time (xerces),
    // reversing order of addition fixes the problem
    calendar.add(Calendar.SECOND, duration.getSeconds() * duration.getSign());
    calendar.add(Calendar.MINUTE, duration.getMinutes() * duration.getSign());
    calendar.add(Calendar.HOUR, duration.getHours() * duration.getSign());
    calendar.add(Calendar.DAY_OF_MONTH, duration.getDays() * duration.getSign());
    calendar.add(Calendar.MONTH, duration.getMonths() * duration.getSign());
    calendar.add(Calendar.YEAR, duration.getYears() * duration.getSign());

    return calendar;
  }

  private Calendar parseDate(String date) throws Exception {
    return ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.forTimeZone(clockReader.getCurrentTimeZone())).parseDateTime(date).toCalendar(null);
  }

  private Duration parsePeriod(String period) throws Exception {
    return datatypeFactory.newDuration(period);
  }

  private boolean isDuration(String time) {
    return time.startsWith("P");
  }

}
