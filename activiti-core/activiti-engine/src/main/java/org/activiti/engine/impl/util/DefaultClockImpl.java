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
package org.activiti.engine.impl.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**

 */
public class DefaultClockImpl implements org.activiti.engine.runtime.Clock {

  private static volatile Calendar CURRENT_TIME;

  @Override
  public void setCurrentTime(Date currentTime) {
    Calendar time = null;

    if (currentTime != null) {
      time = new GregorianCalendar();
      time.setTime(currentTime);
    }

    setCurrentCalendar(time);
  }

  @Override
  public void setCurrentCalendar(Calendar currentTime) {
    CURRENT_TIME = currentTime;
  }

  @Override
  public void reset() {
    CURRENT_TIME = null;
  }

  @Override
  public Date getCurrentTime() {
    return CURRENT_TIME == null ? new Date() : CURRENT_TIME.getTime();
  }

  @Override
  public Calendar getCurrentCalendar() {
    return CURRENT_TIME == null ? new GregorianCalendar() : (Calendar) CURRENT_TIME.clone();
  }

  @Override
  public Calendar getCurrentCalendar(TimeZone timeZone) {
    return TimeZoneUtil.convertToTimeZone(getCurrentCalendar(), timeZone);
  }

  @Override
  public TimeZone getCurrentTimeZone() {
    return getCurrentCalendar().getTimeZone();
  }

}
