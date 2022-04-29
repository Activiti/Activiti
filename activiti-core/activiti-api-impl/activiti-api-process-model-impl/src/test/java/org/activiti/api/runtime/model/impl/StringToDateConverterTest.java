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

package org.activiti.api.runtime.model.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StringToDateConverterTest {

    private static Instant example = Instant.parse("2022-01-17T00:00:00Z");

    private static Stream<Arguments> arguments() {
        return Stream.of(
            Arguments.of("2022-01-17T00:00:00.000Z", example),
            Arguments.of("2022-01-17T00:00:00Z", example),
            Arguments.of("2022-01-17T00:00:00.000-00:00", example),
            Arguments.of("2022-01-17T00:00:00-00:00", example)
        );
    }

    private StringToDateConverter subject = new StringToDateConverter();

    @ParameterizedTest
    @MethodSource("arguments")
    public void convert(String source, Instant expected) {
        //when
        Date result = subject.convert(source);

        //then
        assertThat(result.toInstant()).isEqualTo(expected);
    }

}
