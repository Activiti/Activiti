package org.activiti.standalone.calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.activiti.engine.impl.calendar.AdvancedCycleBusinessCalendar;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;


public class AdvancedCycleBusinessCalendarTest extends PvmTestCase {

  private static final Clock testingClock = new DefaultClockImpl();
  
  public void testDaylightSavingFallIso() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20131103-04:00:00", TimeZone.getTimeZone("UTC")));
    
    assertEquals(parseCalendar("20131104-05:00:00", TimeZone.getTimeZone("UTC")).getTime(), businessCalendar.resolveDuedate("R2/2013-11-03T00:00:00-04:00/P1D DSTZONE:US/Eastern"));
  }

  public void testDaylightSavingSpringIso() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140309-05:00:00", TimeZone.getTimeZone("UTC")));
    
    assertEquals(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")).getTime(), businessCalendar.resolveDuedate("R2/2014-03-09T00:00:00-05:00/P1D DSTZONE:US/Eastern"));
  }

  public void testIsoString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")));
    
    assertEquals(parseCalendar("20140311-04:00:00", TimeZone.getTimeZone("UTC")).getTime(), businessCalendar.resolveDuedate("R2/2014-03-10T04:00:00/P1D DSTZONE:US/Eastern"));
  }

  public void testLegacyIsoString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getDefault()));
    
    assertEquals(parseCalendar("20140311-00:00:00", TimeZone.getDefault()).getTime(), businessCalendar.resolveDuedate("R2/2014-03-10T00:00:00/P1D"));
  }
  
  public void testDaylightSavingFallCron() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20131103-04:00:00", TimeZone.getTimeZone("UTC")));
    
    assertEquals(parseCalendar("20131103-17:00:00", TimeZone.getTimeZone("UTC")).getTime(), businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern"));
  }

  public void testDaylightSavingSpringCron() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140309-05:00:00", TimeZone.getTimeZone("UTC")));
    
    assertEquals(parseCalendar("20140309-16:00:00", TimeZone.getTimeZone("UTC")).getTime(), businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern"));
  }

  public void testCronString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")));
    
    assertEquals(parseCalendar("20140310-16:00:00", TimeZone.getTimeZone("UTC")).getTime(), businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern"));
  }

  public void testLegacyCronString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")));
    
    assertEquals(parseCalendar("20140310-12:00:00", TimeZone.getTimeZone("UTC")).getTime(), businessCalendar.resolveDuedate("0 0 12 1/1 * ? *"));
  }
  
  private Calendar parseCalendar(String str, TimeZone timeZone) throws Exception {
    return parseCalendar(str, timeZone, "yyyyMMdd-HH:mm:ss");
  }

  private Calendar parseCalendar(String str, TimeZone timeZone, String format) throws Exception {
    Calendar date = new GregorianCalendar(timeZone);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
    simpleDateFormat.setTimeZone(timeZone);
    date.setTime(simpleDateFormat.parse(str));
    return date;
  }
  
}
