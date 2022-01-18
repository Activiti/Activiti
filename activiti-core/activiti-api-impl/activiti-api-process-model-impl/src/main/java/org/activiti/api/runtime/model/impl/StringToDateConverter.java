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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.core.convert.converter.Converter;

@ProcessVariableTypeConverter
public class StringToDateConverter implements Converter<String, Date> {

    private static SimpleDateFormat ISO_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    static {
      ISO_DATE_TIME.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Date convert(String source) {
        try {
            return ISO_DATE_TIME.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
