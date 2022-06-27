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


package org.activiti.engine.test.impl.calendar;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.MapBusinessCalendarManager;

import junit.framework.TestCase;

/**
 */
public class MapBusinessCalendarManagerTest extends TestCase {

  public void testMapConstructor() {
    Map<String, BusinessCalendar> calendars = new HashMap<String, BusinessCalendar>(1);
    CycleBusinessCalendar calendar = new CycleBusinessCalendar(null);
    calendars.put("someKey", calendar);
    MapBusinessCalendarManager businessCalendarManager = new MapBusinessCalendarManager(calendars);

    assertThat(businessCalendarManager.getBusinessCalendar("someKey").equals(calendar)).isTrue();
  }

  public void testInvalidCalendarNameRequest() {
    MapBusinessCalendarManager businessCalendarManager = new MapBusinessCalendarManager(emptyMap());

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> businessCalendarManager.getBusinessCalendar("INVALID"))
      .withMessageContaining("INVALID does not exist");
  }

  public void testNullCalendars() {
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> new MapBusinessCalendarManager(null));
  }

}
