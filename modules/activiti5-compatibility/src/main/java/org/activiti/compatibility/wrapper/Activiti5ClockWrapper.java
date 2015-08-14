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
package org.activiti.compatibility.wrapper;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.activiti.engine.runtime.Clock;

/**
 * @author Tijs Rademakers
 */
public class Activiti5ClockWrapper implements Clock {
  
  protected org.activiti5.engine.runtime.Clock activiti5Clock;
  
  public Activiti5ClockWrapper(org.activiti5.engine.runtime.Clock activiti5Clock) {
    this.activiti5Clock = activiti5Clock;
  }

  @Override
  public void setCurrentTime(Date currentTime) {
    activiti5Clock.setCurrentTime(currentTime);
  }

  @Override
  public void setCurrentCalendar(Calendar currentTime) {
    activiti5Clock.setCurrentCalendar(currentTime);
  }

  @Override
  public void reset() {
    activiti5Clock.reset();
  }

  @Override
  public Date getCurrentTime() {
    return activiti5Clock.getCurrentTime();
  }

  @Override
  public Calendar getCurrentCalendar() {
    return activiti5Clock.getCurrentCalendar();
  }

  @Override
  public Calendar getCurrentCalendar(TimeZone timeZone) {
    return activiti5Clock.getCurrentCalendar(timeZone);
  }

  @Override
  public TimeZone getCurrentTimeZone() {
    return activiti5Clock.getCurrentTimeZone();
  }

}
