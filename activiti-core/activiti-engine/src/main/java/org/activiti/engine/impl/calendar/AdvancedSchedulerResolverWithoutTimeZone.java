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
package org.activiti.engine.impl.calendar;

import java.util.Date;
import java.util.TimeZone;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.runtime.ClockReader;

/**
 * Resolves a due date using the original Activiti due date resolver. This does not take into account the passed time zone.
 *
 */
@Internal
public class AdvancedSchedulerResolverWithoutTimeZone implements AdvancedSchedulerResolver {

  @Override
  public Date resolve(String duedateDescription, ClockReader clockReader, TimeZone timeZone) {
    return new CycleBusinessCalendar(clockReader).resolveDuedate(duedateDescription);
  }

}
