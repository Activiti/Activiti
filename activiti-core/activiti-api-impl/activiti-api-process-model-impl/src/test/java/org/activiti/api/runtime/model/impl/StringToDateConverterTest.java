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

package org.activiti.api.runtime.model.impl;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class StringToDateConverterTest {

    private StringToDateConverter subject = new StringToDateConverter();

    @Test
    void convertISODateTime() {
        //given
        String source = "2022-01-17T00:00:00.000Z";

        //when
        Date result = subject.convert(source);

        //then
        assertThat(result).isEqualTo(Instant.parse(source));
    }

    @Test
    void convertISODateTimeOffset() {
        //given
        String source = "2022-01-17T00:00:00.000-00:00";

        //when
        Date result = subject.convert(source);

        //then
        assertThat(result).isEqualTo(Date.from(Instant.parse("2022-01-17T00:00:00.000Z")));
    }

}
