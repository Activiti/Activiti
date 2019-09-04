/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.spring.process.variable;

import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;

public class DateFormatterProvider  {
   
    @Value("${spring.activiti.date-format-pattern.date-format-pattern:yyyy-MM-dd[['T'][ ]HH:mm:ss[.SSS'Z']]}")
    private String dateFormatPattern;
    
    private ZoneId zoneId = ZoneOffset.UTC;
 
    public DateFormatterProvider() {
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
 
    public Date convert2Date(String value) throws DateTimeException { 
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                                                      .appendPattern(getDateFormatPattern())
                                                      .toFormatter()
                                                      .withZone(getZoneId());
        
        try {
            LocalDateTime localDateTime = dateTimeFormatter.parse(value,
                                                                  LocalDateTime::from);
            return Date.from(localDateTime.atZone(getZoneId()).toInstant());                
        } catch (DateTimeException e) {
            LocalDate localDate = dateTimeFormatter.parse(String.valueOf(value),
                                                          LocalDate::from);
            return Date.from(localDate.atStartOfDay().atZone(getZoneId()).toInstant());
        }
    }    
    
    public Date convert2Date(Object value) throws DateTimeException {
        if (value instanceof String) {
            return convert2Date(String.valueOf(value));
        }

        if (value instanceof Date) {
            return (Date)value;
        }
        
        if (value instanceof Long) {
            return new Date((long)value);                       
        }
 
        throw new DateTimeException(MessageFormat.format("Error parsing date/time variable: {0}",value));
    }    
}
