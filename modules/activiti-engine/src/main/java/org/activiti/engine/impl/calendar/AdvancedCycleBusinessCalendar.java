package org.activiti.engine.impl.calendar;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.runtime.ClockReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Activiti BusinessCalendar for cycle based schedules that takes into
 * account a different daylight savings time zone than the one that the server
 * is configured for.
 * <p>
 * For CRON strings DSTZONE is used as the time zone that the CRON schedule
 * refers to. Leave it out to use the server time zone.
 * <p>
 * For ISO strings the time zone offset for the date/time specified is part of
 * the string itself. DSTZONE is used to determine what the offset should be
 * NOW, which may be different than when the workflow was scheduled if it is
 * scheduled to run across a DST event.
 * 
 * <pre>
 *   For example:
 *      R/2013-10-01T20:30:00/P1D DSTZONE:US/Eastern
 *      R/2013-10-01T20:30:00/P1D DSTZONE:UTC
 *      R/2013-10-01T20:30:00/P1D DSTZONE:US/Arizona
 *      0 30 20 ? * MON,TUE,WED,THU,FRI * DSTZONE:US/Eastern
 *      0 30 20 ? * MON,TUE,WED,THU,FRI * DSTZONE:UTC
 *      0 30 20 ? * MON,TUE,WED,THU,FRI * DSTZONE:US/Arizona
 * </pre>
 * 
 * Removing the DSTZONE key will cause Activiti to use the server's time zone.
 * This is the original behavior.
 * <p>
 * Schedule strings are versioned. Version 1 strings will use the original
 * Activiti CycleBusinessCalendar. All new properties are ignored. Version 2
 * strings will use the new daylight saving time logic.
 * 
 * <pre>
 *   For example:
 *      R/2013-10-01T20:30:00/P1D VER:2 DSTZONE:US/Eastern
 *      0 30 20 ? * MON,TUE,WED,THU,FRI * VER:1 DSTZONE:US/Arizona
 * </pre>
 * 
 * By default (if no VER key is included in the string), it assumes version 2.
 * This can be changed by modifying the defaultScheduleVersion property.
 * <p>
 * 
 * @author mseiden
 */
public class AdvancedCycleBusinessCalendar extends CycleBusinessCalendar {

  private Integer defaultScheduleVersion;

  private static final Integer DEFAULT_VERSION = 2;

  private static final Logger logger = LoggerFactory.getLogger(AdvancedCycleBusinessCalendar.class);

  private static final Map<Integer, AdvancedSchedulerResolver> resolvers;

  static {
    resolvers = new ConcurrentHashMap<Integer, AdvancedSchedulerResolver>();
    resolvers.put(1, new AdvancedSchedulerResolverWithoutTimeZone());
    resolvers.put(2, new AdvancedSchedulerResolverWithTimeZone());
  }

  public AdvancedCycleBusinessCalendar(ClockReader clockReader) {
    super(clockReader);
  }

  public AdvancedCycleBusinessCalendar(ClockReader clockReader, Integer defaultScheduleVersion) {
    this(clockReader);
    this.defaultScheduleVersion = defaultScheduleVersion;
  }

  public Integer getDefaultScheduleVersion() {
    return defaultScheduleVersion == null ? DEFAULT_VERSION : defaultScheduleVersion;
  }

  public void setDefaultScheduleVersion(Integer defaultScheduleVersion) {
    this.defaultScheduleVersion = defaultScheduleVersion;
  }

  @Override
  public Date resolveDuedate(String duedateDescription, int maxIterations) {
    logger.info("Resolving Due Date: " + duedateDescription);

    String timeZone = getValueFrom("DSTZONE", duedateDescription);
    String version = getValueFrom("VER", duedateDescription);

    // START is a legacy value that is no longer used, but may still exist in
    // deployed job schedules
    // Could be used in the future as a start date for a CRON job
    // String startDate = getValueFrom("START", duedateDescription);

    duedateDescription = removeValueFrom("VER", removeValueFrom("START", removeValueFrom("DSTZONE", duedateDescription))).trim();

    try {
      logger.info("Base Due Date: " + duedateDescription);

      Date date = resolvers.get(version == null ? getDefaultScheduleVersion() : Integer.valueOf(version)).resolve(duedateDescription, clockReader,
              timeZone == null ? clockReader.getCurrentTimeZone() : TimeZone.getTimeZone(timeZone));

      logger.info("Calculated Date: " + (date == null ? "Will Not Run Again" : date));

      return date;

    } catch (Exception e) {
      throw new ActivitiIllegalArgumentException("Cannot parse duration", e);
    }

  }

  private String getValueFrom(String field, String duedateDescription) {
    int fieldIndex = duedateDescription.indexOf(field + ":");

    if (fieldIndex > -1) {
      int nextWhiteSpace = duedateDescription.indexOf(" ", fieldIndex);

      fieldIndex += field.length() + 1;

      if (nextWhiteSpace > -1) {
        return duedateDescription.substring(fieldIndex, nextWhiteSpace);
      } else {
        return duedateDescription.substring(fieldIndex);
      }
    }

    return null;
  }

  private String removeValueFrom(String field, String duedateDescription) {
    int fieldIndex = duedateDescription.indexOf(field + ":");

    if (fieldIndex > -1) {
      int nextWhiteSpace = duedateDescription.indexOf(" ", fieldIndex);

      if (nextWhiteSpace > -1) {
        return duedateDescription.replace(duedateDescription.substring(fieldIndex, nextWhiteSpace), "");
      } else {
        return duedateDescription.substring(0, fieldIndex);
      }
    }

    return duedateDescription;
  }
}
