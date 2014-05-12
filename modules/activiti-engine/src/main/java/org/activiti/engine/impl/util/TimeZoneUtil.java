package org.activiti.engine.impl.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class TimeZoneUtil {

  public static Calendar convertToTimeZone(Calendar time, TimeZone timeZone) {
    Calendar foreignTime = new GregorianCalendar(timeZone);
    foreignTime.setTimeInMillis(time.getTimeInMillis());

    return foreignTime;
  }

}
