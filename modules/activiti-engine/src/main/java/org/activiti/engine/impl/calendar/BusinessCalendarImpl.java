package org.activiti.engine.impl.calendar;

import org.activiti.engine.runtime.ClockReader;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

/**
 * This class implements business calendar based on internal clock
 */
public abstract class BusinessCalendarImpl implements BusinessCalendar {

  protected ClockReader clockReader;

  public BusinessCalendarImpl(ClockReader clockReader) {
    this.clockReader = clockReader;
  }

  @Override
  public Date resolveDuedate(String duedateDescription){
    return resolveDuedate(duedateDescription,-1);
  }

  public abstract Date resolveDuedate(String duedateDescription, int maxIterations);

  @Override
  public Boolean validateDuedate(String duedateDescription, int maxIterations, Date endDate, Date newTimer) {
    return endDate == null || endDate.after(newTimer) || endDate.equals(newTimer);
  }

  @Override
  public Date resolveEndDate(String endDateString) {
      return ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.forTimeZone(clockReader.getCurrentTimeZone())).parseDateTime(endDateString).toCalendar(null).getTime();
  }

}
