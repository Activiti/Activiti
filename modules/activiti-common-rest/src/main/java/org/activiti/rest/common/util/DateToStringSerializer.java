package org.activiti.rest.common.util;

import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DateToStringSerializer extends JsonSerializer<Date> {

  protected DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTime();
  
  @Override
  public void serialize(Date tmpDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) 
      throws IOException, JsonProcessingException {
    
    if (tmpDate != null) {
      jsonGenerator.writeString(new DateTime(tmpDate).toString(isoFormatter));
    } else {
      jsonGenerator.writeNull();
    }
  }
}
