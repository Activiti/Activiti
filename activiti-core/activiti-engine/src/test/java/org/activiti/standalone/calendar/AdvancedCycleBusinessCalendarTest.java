package org.activiti.standalone.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.activiti.engine.impl.calendar.AdvancedCycleBusinessCalendar;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;
import org.junit.Test;

public class AdvancedCycleBusinessCalendarTest {

  private static final Clock testingClock = new DefaultClockImpl();

  @Test
  public void testDaylightSavingFallIso() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20131103-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("R2/2013-11-03T00:00:00-04:00/P1D DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20131104-05:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  @Test
  public void testDaylightSavingSpringIso() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140309-05:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("R2/2014-03-09T00:00:00-05:00/P1D DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  @Test
  public void testIsoString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("R2/2014-03-10T04:00:00/P1D DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20140311-04:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  @Test
  public void testLegacyIsoString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getDefault()));

    assertThat(businessCalendar.resolveDuedate("R2/2014-03-10T00:00:00/P1D")).isEqualTo(parseCalendar("20140311-00:00:00", TimeZone.getDefault()).getTime());
  }

  @Test
  public void testDaylightSavingFallCron() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20131103-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20131103-17:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  @Test
  public void testDaylightSavingSpringCron() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140309-05:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20140309-16:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  @Test
  public void testCronString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20140310-16:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  @Test
  public void testLegacyCronString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("0 0 12 1/1 * ? *")).isEqualTo(parseCalendar("20140310-12:00:00", TimeZone.getTimeZone("UTC")).getTime());
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
