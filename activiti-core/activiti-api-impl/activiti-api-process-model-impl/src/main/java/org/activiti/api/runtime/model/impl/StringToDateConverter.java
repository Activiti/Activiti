package org.activiti.api.runtime.model.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class StringToDateConverter implements Converter<String, Date> {

    private String dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public StringToDateConverter() {
    }

//    public StringToDateConverter(String dateFormatString) {
//        this.dateFormatString = dateFormatString;
//    }

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