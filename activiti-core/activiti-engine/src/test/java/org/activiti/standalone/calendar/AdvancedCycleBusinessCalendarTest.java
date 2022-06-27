/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.standalone.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.activiti.engine.impl.calendar.AdvancedCycleBusinessCalendar;
import org.activiti.engine.impl.test.AbstractTestCase;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;

public class AdvancedCycleBusinessCalendarTest extends AbstractTestCase {

  private static final Clock testingClock = new DefaultClockImpl();

  public void testDaylightSavingFallIso() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20131103-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("R2/2013-11-03T00:00:00-04:00/P1D DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20131104-05:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  public void testDaylightSavingSpringIso() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140309-05:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("R2/2014-03-09T00:00:00-05:00/P1D DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  public void testIsoString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("R2/2014-03-10T04:00:00/P1D DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20140311-04:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  public void testLegacyIsoString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getDefault()));

    assertThat(businessCalendar.resolveDuedate("R2/2014-03-10T00:00:00/P1D")).isEqualTo(parseCalendar("20140311-00:00:00", TimeZone.getDefault()).getTime());
  }

  public void testDaylightSavingFallCron() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20131103-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20131103-17:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  public void testDaylightSavingSpringCron() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140309-05:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20140309-16:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

  public void testCronString() throws Exception {
    AdvancedCycleBusinessCalendar businessCalendar = new AdvancedCycleBusinessCalendar(testingClock);

    testingClock.setCurrentCalendar(parseCalendar("20140310-04:00:00", TimeZone.getTimeZone("UTC")));

    assertThat(businessCalendar.resolveDuedate("0 0 12 1/1 * ? * DSTZONE:US/Eastern")).isEqualTo(parseCalendar("20140310-16:00:00", TimeZone.getTimeZone("UTC")).getTime());
  }

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
