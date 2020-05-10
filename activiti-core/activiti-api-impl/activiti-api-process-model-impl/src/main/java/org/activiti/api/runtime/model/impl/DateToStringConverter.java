package org.activiti.api.runtime.model.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class DateToStringConverter implements Converter<Date, String> {

    private String dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public DateToStringConverter() {
    }

    public DateToStringConverter(String dateFormatString) {
        this.dateFormatString = dateFormatString;
    }

    @Override
    public String convert(Date source) {
        DateFormat df = new SimpleDateFormat(dateFormatString);

        return df.format(source);
    }
}