package org.activiti.engine.impl.calendar;

import java.util.Date;
import java.util.TimeZone;

/**
 * Provides an interface for versioned due date resolvers.
 * 
 * @author mseiden
 */
public interface AdvancedSchedulerResolver {

  /**
   * Resolves a due date using the specified time zone (if supported)
   * 
   * @param duedateDescription
   *          An original Activiti schedule string in either ISO or CRON format
   * @param timeZone
   *          The time zone to use in the calculations
   * @return The due date
   */
  Date resolve(String duedateDescription, TimeZone timeZone);

}
