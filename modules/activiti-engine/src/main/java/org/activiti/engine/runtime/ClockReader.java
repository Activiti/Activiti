package org.activiti.engine.runtime;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This interface provides clock reading functionality
 */
public interface ClockReader {

  Date getCurrentTime();

  Calendar getCurrentCalendar();
  
  Calendar getCurrentCalendar(TimeZone timeZone);
  
  TimeZone getCurrentTimeZone();

}
