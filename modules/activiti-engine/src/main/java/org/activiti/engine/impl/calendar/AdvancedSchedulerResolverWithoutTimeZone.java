package org.activiti.engine.impl.calendar;

import java.util.Date;
import java.util.TimeZone;

import org.activiti.engine.runtime.ClockReader;

/**
 * Resolves a due date using the original Activiti due date resolver. This does
 * not take into account the passed time zone.
 * 
 * @author mseiden
 */
public class AdvancedSchedulerResolverWithoutTimeZone implements AdvancedSchedulerResolver {

  @Override
  public Date resolve(String duedateDescription, ClockReader clockReader, TimeZone timeZone) {
    return new CycleBusinessCalendar(clockReader).resolveDuedate(duedateDescription);
  }

}
