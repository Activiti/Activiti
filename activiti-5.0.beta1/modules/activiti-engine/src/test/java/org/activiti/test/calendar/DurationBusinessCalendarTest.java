/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.activiti.test.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.pvm.test.PvmTestCase;

/**
 * @author Tom Baeyens
 */
public class DurationBusinessCalendarTest extends PvmTestCase {

  public void testSimpleDuration() throws Exception {
    DurationBusinessCalendar businessCalendar = new DurationBusinessCalendar();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMM dd - HH:mm");
    Date now = simpleDateFormat.parse("2010 Jun 11 - 17:23");
    ClockUtil.setCurrentTime(now);

    Date duedate = businessCalendar.resolveDuedate("P2DT5H70M");

    Date expectedDuedate = simpleDateFormat.parse("2010 Jun 13 - 23:33");

    assertEquals(expectedDuedate, duedate);
  }

}
