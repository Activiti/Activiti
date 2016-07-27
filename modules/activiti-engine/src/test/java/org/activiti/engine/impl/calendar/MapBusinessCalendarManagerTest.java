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

package org.activiti.engine.impl.calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.activiti.engine.ActivitiException;

/**
 * Created by martin.grofcik
 */
public class MapBusinessCalendarManagerTest extends TestCase {

  public void testMapConstructor() {
    Map<String, BusinessCalendar> calendars = new HashMap<String, BusinessCalendar>(1);
    CycleBusinessCalendar calendar = new CycleBusinessCalendar(null);
    calendars.put("someKey", calendar);
    MapBusinessCalendarManager businessCalendarManager = new MapBusinessCalendarManager(calendars);

    assertTrue(businessCalendarManager.getBusinessCalendar("someKey").equals(calendar));
  }

  public void testInvalidCalendarNameRequest() {
    @SuppressWarnings("unchecked") MapBusinessCalendarManager businessCalendarManager = new MapBusinessCalendarManager(Collections.EMPTY_MAP);

    try {
      businessCalendarManager.getBusinessCalendar("INVALID");
      fail("ActivitiException expected");
    } catch (ActivitiException e) {
      assertThat(e.getMessage(), containsString("INVALID does not exist"));
    }
  }

  public void testNullCalendars() {
    try {
      new MapBusinessCalendarManager(null);
      fail("AssertionError expected");
    } catch(IllegalArgumentException e) {
      // Expected error
    }
  }
}