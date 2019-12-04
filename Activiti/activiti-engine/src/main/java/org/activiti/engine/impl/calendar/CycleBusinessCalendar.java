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

import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.runtime.ClockReader;

@Internal
public class CycleBusinessCalendar extends BusinessCalendarImpl {

  public static String NAME = "cycle";

  public CycleBusinessCalendar(ClockReader clockReader) {
    super(clockReader);
  }

  public Date resolveDuedate(String duedateDescription, int maxIterations) {
    try {
      if (duedateDescription != null && duedateDescription.startsWith("R")) {
        return new DurationHelper(duedateDescription, maxIterations, clockReader).getDateAfter();
      } else {
        CronExpression ce = new CronExpression(duedateDescription, clockReader);
        return ce.getTimeAfter(clockReader.getCurrentTime());
      }

    } catch (Exception e) {
      throw new ActivitiException("Failed to parse cron expression: " + duedateDescription, e);
    }

  }

  public Boolean validateDuedate(String duedateDescription, int maxIterations, Date endDate, Date newTimer) {
    if (endDate != null) {
      return super.validateDuedate(duedateDescription, maxIterations, endDate, newTimer);
    }
    // end date could be part of the chron expression
    try {
      if (duedateDescription != null && duedateDescription.startsWith("R")) {
        return new DurationHelper(duedateDescription, maxIterations, clockReader).isValidDate(newTimer);
      } else {
        return true;
      }

    } catch (Exception e) {
      throw new ActivitiException("Failed to parse cron expression: " + duedateDescription, e);
    }

  }

}
