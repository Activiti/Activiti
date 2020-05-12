/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.api.runtime.model.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

@ProcessVariableTypeConverter
public class StringToDateConverter implements Converter<String, Date> {

    private String dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public StringToDateConverter() {
    }

    public StringToDateConverter(String dateFormatString) {
        this.dateFormatString = dateFormatString;
    }

    @Override
    public Date convert(String source) {
        DateFormat df = new SimpleDateFormat(dateFormatString);

        try {
            return df.parse(source);
        } catch (ParseException cause) {
            throw new RuntimeException(cause);
        }
    }
}