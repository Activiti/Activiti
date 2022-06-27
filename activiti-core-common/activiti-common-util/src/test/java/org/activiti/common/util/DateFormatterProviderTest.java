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
package org.activiti.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class DateFormatterProviderTest {

    private DateFormatterProvider provider = new DateFormatterProvider("yyyy-MM-dd[['T']HH:mm:ss[.SSS'Z']]");

    @Test
    public void should_returnDate_when_stringRepresentsADate() {

        String dateStr = "1970-01-01";

        Date date = provider.toDate(dateStr);

        assertThat(date).hasTime(0);
    }

    @Test
    public void should_returnDate_when_stringRepresentsADateWithTimeInformation() {

        String dateStr = "1970-01-01T01:01:01.001Z";
        //calculate number of milliseconds after 1970-01-01T00:00:00.000Z
        long time = Duration.ofHours(1).toMillis() + Duration.ofMinutes(1).toMillis() + Duration.ofSeconds(1).toMillis() + 1;

        Date date = provider.toDate(dateStr);

        assertThat(date).hasTime(time);
    }

    @Test
    public void should_throwException_when_stringIsNotADate() {

        String dateStr = "this is not a date";

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> provider.parse(dateStr));
    }

    @Test
    public void should_returnDate_when_longIsProvided() {

        long time = 1000;

        Date date = provider.toDate(time);

        assertThat(date).hasTime(time);
    }

    @Test
    public void should_returnDate_when_dateIsProvided() {

        Date initialDate = new Date(1000);

        Date date = provider.toDate(initialDate);

        assertThat(date).isEqualTo(initialDate);
    }

    @Test
    public void should_throwException_when_isNotAStringADateOrALong() {
        double value = 1.2;

        assertThatExceptionOfType(DateTimeException.class)
                .isThrownBy(
                        () -> provider.toDate(value))
                .withMessageContaining("Error while parsing date");
    }
}
