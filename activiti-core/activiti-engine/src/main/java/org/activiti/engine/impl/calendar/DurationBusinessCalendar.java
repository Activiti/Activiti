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
public class DurationBusinessCalendar extends BusinessCalendarImpl {

  public static String NAME = "duration";

  public DurationBusinessCalendar(ClockReader clockReader) {
    super(clockReader);
  }

  public Date resolveDuedate(String duedate, int maxIterations) {
    try {
      DurationHelper dh = new DurationHelper(duedate, clockReader);
      return dh.getDateAfter();
    } catch (Exception e) {
      throw new ActivitiException("couldn't resolve duedate: " + e.getMessage(), e);
    }
  }

}
