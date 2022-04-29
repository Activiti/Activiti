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

import static java.util.Arrays.asList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.util.TimeZoneUtil;
import org.activiti.engine.runtime.ClockReader;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

@Internal
public class DurationHelper {

    private Calendar start;
    private Calendar end;
    private Duration period;
    private boolean isRepeat;
    private int times;
    private int maxIterations = -1;
    private boolean repeatWithNoBounds;

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

    public DurationHelper(String expressionS,
                          int maxIterations,
                          ClockReader clockReader) throws Exception {
        this.clockReader = clockReader;
        this.maxIterations = maxIterations;
        List<String> expression = asList(expressionS.split("/"));
        datatypeFactory = DatatypeFactory.newInstance();

        if (expression.size() > 3 || expression.isEmpty()) {
            throw new ActivitiIllegalArgumentException("Cannot parse duration");
        }
        if (expression.get(0).startsWith("R")) {
            isRepeat = true;
            times = expression.get(0).length() == 1 ? Integer.MAX_VALUE - 1 : Integer.parseInt(expression.get(0).substring(1));

            if (expression.get(0).equals("R")) { // R without params
                repeatWithNoBounds = true;
            }

            expression = expression.subList(1,
                                            expression.size());
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
        if (start == null) {
            start = clockReader.getCurrentCalendar();
        }
    }

    public DurationHelper(String expressionS,
                          ClockReader clockReader) throws Exception {
        this(expressionS,
             -1,
             clockReader);
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
        return add(start,
                   period);
    }

    public Boolean isValidDate(Date newTimer) {
        return end == null || end.getTime().after(newTimer) || end.getTime().equals(newTimer);
    }

    public Date getDateAfter() {
        Calendar date = getCalendarAfter();

        return date == null ? null : date.getTime();
    }

    private Calendar getDateAfterRepeat(Calendar date) {
        Calendar current = TimeZoneUtil.convertToTimeZone(start,
                                                          date.getTimeZone());

        if (repeatWithNoBounds) {

            while (current.before(date) || current.equals(date)) { // As long as current date is not past the engine date, we keep looping
                Calendar newTime = add(current,
                                       period);
                if (newTime.equals(current) || newTime.before(current)) {
                    break;
                }
                current = newTime;
            }
        } else {

            int maxLoops = times;
            if (maxIterations > 0) {
                maxLoops = maxIterations - times;
            }
            for (int i = 0; i < maxLoops + 1 && !current.after(date); i++) {
                current = add(current,
                              period);
            }
        }
        return current.before(date) ? date : TimeZoneUtil.convertToTimeZone(current,
                                                                            clockReader.getCurrentTimeZone());
    }

    protected Calendar add(Calendar date,
                           Duration duration) {
        Calendar calendar = (Calendar) date.clone();

        // duration.addTo does not account for daylight saving time (xerces),
        // reversing order of addition fixes the problem
        calendar.add(Calendar.SECOND,
                     duration.getSeconds() * duration.getSign());
        calendar.add(Calendar.MINUTE,
                     duration.getMinutes() * duration.getSign());
        calendar.add(Calendar.HOUR,
                     duration.getHours() * duration.getSign());
        calendar.add(Calendar.DAY_OF_MONTH,
                     duration.getDays() * duration.getSign());
        calendar.add(Calendar.MONTH,
                     duration.getMonths() * duration.getSign());
        calendar.add(Calendar.YEAR,
                     duration.getYears() * duration.getSign());

        return calendar;
    }

    protected Calendar parseDate(String date) throws Exception {
        Calendar dateCalendar = null;
        try {
            dateCalendar = ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.forTimeZone(
                    clockReader.getCurrentTimeZone())).parseDateTime(date).toCalendar(null);
        } catch (IllegalArgumentException e) {
            // try to parse a java.util.date to string back to a java.util.date
            dateCalendar = new GregorianCalendar();
            DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy",
                                                          Locale.ENGLISH);
            dateCalendar.setTime(DATE_FORMAT.parse(date));
        }

        return dateCalendar;
    }

    protected Duration parsePeriod(String period) throws Exception {
        return datatypeFactory.newDuration(period);
    }

    protected boolean isDuration(String time) {
        return time.startsWith("P");
    }
}
