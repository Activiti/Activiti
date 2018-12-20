package org.activiti.spring.process.variable.types;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic date type for setting default date values for vars in extension json
 */
public class DateVariableType extends JavaObjectVariableType {

    public static String defaultFormat = "yyyy-MM-dd";
    public DateFormat format;
    private static final Logger logger = LoggerFactory.getLogger(DateVariableType.class);


    public DateVariableType(Class clazz, DateFormat format) {
        super(clazz);
        this.format = format;
    }

    public DateFormat getFormat() {
        return format;
    }

    public void setFormat(DateFormat format) {
        this.format = format;
    }

    @Override
    public void validate(Object var, List<ActivitiException> errors) {
        super.validate(var,errors);
    }

    @Override
    public Object parseFromValue(Object value) throws ActivitiException {

        try {
            return format.parse(String.valueOf(value));
        } catch (ParseException e) {
            throw new ActivitiException("Error parsing date value " + value, e);
        }
    }
}
