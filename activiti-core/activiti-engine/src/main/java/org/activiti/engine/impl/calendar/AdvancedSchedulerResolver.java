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
 * Provides an interface for versioned due date resolvers.
 *
 */
@Internal
public interface AdvancedSchedulerResolver {

  /**
   * Resolves a due date using the specified time zone (if supported)
   *
   * @param duedateDescription
   *          An original Activiti schedule string in either ISO or CRON format
   * @param clockReader
   *          The time provider
   * @param timeZone
   *          The time zone to use in the calculations
   * @return The due date
   */
  Date resolve(String duedateDescription, ClockReader clockReader, TimeZone timeZone);

}
