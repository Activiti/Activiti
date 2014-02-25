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

import org.activiti.engine.impl.calendar.DurationHelper;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;
import org.junit.After;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static groovy.util.GroovyTestCase.assertEquals;
import static org.junit.Assert.assertNull;

public class DurationHelperTest {

  @Test
  public void shouldNotExceedNumber() throws Exception {
    Clock testingClock = new DefaultClockImpl();
    testingClock.setCurrentTime(new Date(0));
    DurationHelper dh = new DurationHelper("R2/PT10S", testingClock);

    testingClock.setCurrentTime(new Date(15000));
    assertEquals(20000, dh.getDateAfter().getTime());

    testingClock.setCurrentTime(new Date(30000));
    assertNull(dh.getDateAfter());
  }

  @Test
  public void shouldNotExceedNumberPeriods() throws Exception {
    Clock testingClock = new DefaultClockImpl();
    testingClock.setCurrentTime(parse("19700101-00:00:00"));
    DurationHelper dh = new DurationHelper("R2/1970-01-01T00:00:00/1970-01-01T00:00:10", testingClock);

    testingClock.setCurrentTime(parse("19700101-00:00:15"));
    assertEquals(parse("19700101-00:00:20"), dh.getDateAfter());


    testingClock.setCurrentTime(parse("19700101-00:00:30"));
    assertNull(dh.getDateAfter());
  }

  @Test
  public void shouldNotExceedNumberNegative() throws Exception {
    Clock testingClock = new DefaultClockImpl();
    testingClock.setCurrentTime(parse("19700101-00:00:00"));
    DurationHelper dh = new DurationHelper("R2/PT10S/1970-01-01T00:00:50", testingClock);

    testingClock.setCurrentTime(parse("19700101-00:00:20"));
    assertEquals(parse("19700101-00:00:30"), dh.getDateAfter());


    testingClock.setCurrentTime(parse("19700101-00:00:35"));

    assertEquals(parse("19700101-00:00:40"), dh.getDateAfter());
  }


  private Date parse(String str) throws Exception {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    return simpleDateFormat.parse(str);
  }


}
