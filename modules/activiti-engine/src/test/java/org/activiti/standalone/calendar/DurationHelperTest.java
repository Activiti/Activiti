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
 */

package org.activiti.standalone.calendar;

import static groovy.util.GroovyTestCase.assertEquals;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.activiti.engine.impl.calendar.DurationHelper;
import org.activiti.engine.impl.util.ClockUtil;
import org.junit.After;
import org.junit.Test;

public class DurationHelperTest {
  
  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @Test
  public void shouldNotExceedNumber() throws Exception {
    ClockUtil.setCurrentTime(new Date(0));
    DurationHelper dh = new DurationHelper("R2/PT10S");

    ClockUtil.setCurrentTime(new Date(15000));
    assertEquals(20000, dh.getDateAfter().getTime());


    ClockUtil.setCurrentTime(new Date(30000));
    assertNull(dh.getDateAfter());
  }

  @Test
  public void shouldNotExceedNumberPeriods() throws Exception {
    ClockUtil.setCurrentTime(parse("19700101-00:00:00"));
    DurationHelper dh = new DurationHelper("R2/1970-01-01T00:00:00/1970-01-01T00:00:10");

    ClockUtil.setCurrentTime(parse("19700101-00:00:15"));
    assertEquals(parse("19700101-00:00:20"), dh.getDateAfter());


    ClockUtil.setCurrentTime(parse("19700101-00:00:30"));
    assertNull(dh.getDateAfter());
  }

  @Test
  public void shouldNotExceedNumberNegative() throws Exception {
    ClockUtil.setCurrentTime(parse("19700101-00:00:00"));
    DurationHelper dh = new DurationHelper("R2/PT10S/1970-01-01T00:00:50");

    ClockUtil.setCurrentTime(parse("19700101-00:00:20"));
    assertEquals(parse("19700101-00:00:30"), dh.getDateAfter());


    ClockUtil.setCurrentTime(parse("19700101-00:00:35"));

    assertEquals(parse("19700101-00:00:40"), dh.getDateAfter());
  }

  @Test
  public void daylightSavingFall() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20131103-04:45:00", TimeZone.getTimeZone("UTC")));

    DurationHelper dh = new DurationHelper("R2/2013-11-03T00:45:00-04:00/PT1H");

    assertEquals(parseCalendar("20131103-05:45:00", TimeZone.getTimeZone("UTC")), dh.getCalendarAfter(ClockUtil.getCurrentCalendar(TimeZone.getTimeZone("US/Eastern"))));

    ClockUtil.setCurrentCalendar(parseCalendar("20131103-05:45:00", TimeZone.getTimeZone("UTC")));

    assertEquals(parseCalendar("20131103-06:45:00", TimeZone.getTimeZone("UTC")), dh.getCalendarAfter(ClockUtil.getCurrentCalendar(TimeZone.getTimeZone("US/Eastern"))));
}

  @Test
  public void daylightSavingFallFirstHour() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20131103-05:45:00", TimeZone.getTimeZone("UTC")));
    Calendar easternTime = ClockUtil.getCurrentCalendar(TimeZone.getTimeZone("US/Eastern"));

    DurationHelper dh = new DurationHelper("R2/2013-11-03T01:45:00-04:00/PT1H");

    assertEquals(parseCalendar("20131103-06:45:00", TimeZone.getTimeZone("UTC")), dh.getCalendarAfter(easternTime));
  }

  @Test
  public void daylightSavingFallSecondHour() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20131103-06:45:00", TimeZone.getTimeZone("UTC")));
    Calendar easternTime = ClockUtil.getCurrentCalendar(TimeZone.getTimeZone("US/Eastern"));

    DurationHelper dh = new DurationHelper("R2/2013-11-03T01:45:00-05:00/PT1H");

    assertEquals(parseCalendar("20131103-07:45:00", TimeZone.getTimeZone("UTC")), dh.getCalendarAfter(easternTime));
  }

  
  @Test
  public void daylightSavingFallObservedFirstHour() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20131103-00:45:00", TimeZone.getTimeZone("US/Eastern")));

    DurationHelper dh = new DurationHelper("R2/2013-11-03T00:45:00-04:00/PT1H");
    Calendar expected = parseCalendarWithOffset("20131103-01:45:00 -04:00", TimeZone.getTimeZone("US/Eastern"));

    assertEquals(expected, dh.getCalendarAfter());
  }
  
  @Test
  public void daylightSavingFallObservedSecondHour() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20131103-00:45:00", TimeZone.getTimeZone("US/Eastern")));

    DurationHelper dh = new DurationHelper("R2/2013-11-03T00:45:00-04:00/PT2H");
    Calendar expected = parseCalendarWithOffset("20131103-01:45:00 -05:00", TimeZone.getTimeZone("US/Eastern"));

    assertEquals(expected, dh.getCalendarAfter());
  }

  @Test
  public void daylightSavingSpring() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20140309-05:45:00", TimeZone.getTimeZone("UTC")));

    DurationHelper dh = new DurationHelper("R2/2014-03-09T00:45:00-05:00/PT1H");

    assertEquals(parseCalendar("20140309-06:45:00", TimeZone.getTimeZone("UTC")), dh.getCalendarAfter(ClockUtil.getCurrentCalendar(TimeZone.getTimeZone("US/Eastern"))));
}

  @Test
  public void daylightSavingSpringObserved() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20140309-01:45:00", TimeZone.getTimeZone("US/Eastern")));

    DurationHelper dh = new DurationHelper("R2/2014-03-09T01:45:00/PT1H");
    Calendar expected = parseCalendar("20140309-03:45:00", TimeZone.getTimeZone("US/Eastern"));

    assertEquals(expected, dh.getCalendarAfter());
  }

  @Test
  public void daylightSaving25HourDay() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20131103-00:00:00", TimeZone.getTimeZone("US/Eastern")));

    DurationHelper dh = new DurationHelper("R2/2013-11-03T00:00:00/P1D");

    assertEquals(parseCalendar("20131104-00:00:00", TimeZone.getTimeZone("US/Eastern")), dh.getCalendarAfter(ClockUtil.getCurrentCalendar()));
  }

  @Test
  public void daylightSaving23HourDay() throws Exception {
    ClockUtil.setCurrentCalendar(parseCalendar("20140309-00:00:00", TimeZone.getTimeZone("US/Eastern")));

    DurationHelper dh = new DurationHelper("R2/2014-03-09T00:00:00/P1D");

    assertEquals(parseCalendar("20140310-00:00:00", TimeZone.getTimeZone("US/Eastern")), dh.getCalendarAfter(ClockUtil.getCurrentCalendar()));
  }
  
  
  private Date parse(String str) throws Exception {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    return simpleDateFormat.parse(str);
  }

  private Calendar parseCalendarWithOffset(String str, TimeZone timeZone) throws Exception {
    return parseCalendar(str, timeZone, "yyyyMMdd-HH:mm:ss X");
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
