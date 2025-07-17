/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.common.util;

import java.text.MessageFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

public class DateFormatterProvider  {

    private String dateFormatPattern;

    private ZoneId zoneId = ZoneOffset.UTC;

    public DateFormatterProvider(String dateFormatPattern) {
        this.dateFormatPattern = dateFormatPattern;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public String getDateFormatPattern() {
        return dateFormatPattern;
    }

    public void setDateFormatPattern(String dateFormatPattern) {
        this.dateFormatPattern = dateFormatPattern;
    }

    public Date parse(String value) throws DateTimeException {
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                                                      .appendPattern(getDateFormatPattern())
                                                      .toFormatter()
                                                      .withZone(getZoneId());

        try {
            ZonedDateTime zonedDateTime = dateTimeFormatter.parse(value,
                                                                  ZonedDateTime::from);
            return Date.from(zonedDateTime.toInstant());
        } catch (DateTimeException e) {
            LocalDate localDate = dateTimeFormatter.parse(String.valueOf(value),
                                                          LocalDate::from);
            return Date.from(localDate.atStartOfDay().atZone(getZoneId()).toInstant());
        }
    }

    public Date toDate(Object value) {
        if (value instanceof String) {
            return parse((String) value);
        }

        if (value instanceof Date) {
            return (Date)value;
        }

        if (value instanceof Long) {
            return new Date((long)value);
        }

        if (value instanceof LocalDate) {
            return Date.from(((LocalDate)value).atStartOfDay(getZoneId()).toInstant());
        }

        if (value instanceof LocalDateTime) {
            return Date.from(((LocalDateTime)value).atZone(getZoneId()).toInstant());
        }

        if (value instanceof ZonedDateTime) {
            return Date.from(((ZonedDateTime)value).toInstant());
        }

        throw new DateTimeException(MessageFormat.format("Error while parsing date. Type: {0}, value: {1}", value.getClass().getName(), value));
    }
}
