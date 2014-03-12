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
 * @author Joram Barrez
 */
public class ClockUtil {
  
  private volatile static Calendar CURRENT_TIME = null;
  
  public static void setCurrentCalendar(Calendar currentTime) {
    ClockUtil.CURRENT_TIME = currentTime;
  }
  
  public static void setCurrentTime(Date currentTime) {
    Calendar time = null;
    
    if (currentTime != null) {
      time = new GregorianCalendar();
      time.setTime(currentTime);
    }
    
    setCurrentCalendar(time);
  }
  
  public static void reset() {
    ClockUtil.CURRENT_TIME = null;
  } 

  public static Calendar getCurrentCalendar() {
    return CURRENT_TIME == null ? new GregorianCalendar() : (Calendar)CURRENT_TIME.clone(); 
  }

  public static Calendar getCurrentCalendar(TimeZone timeZone) {
    return convertToTimeZone(getCurrentCalendar(), timeZone);
  }
  
  public static TimeZone getCurrentTimeZone() {
    return getCurrentCalendar().getTimeZone();
  }
  
  public static Date getCurrentTime() {
    return CURRENT_TIME == null ? new Date() : CURRENT_TIME.getTime(); 
  }

  public static Calendar convertToTimeZone(Calendar time, TimeZone timeZone) {
    Calendar foreignTime = new GregorianCalendar(timeZone);
    foreignTime.setTimeInMillis(time.getTimeInMillis());

    return foreignTime;
  }

  public static Calendar convertToTimeZone(Calendar time) {
    return convertToTimeZone(time, getCurrentTimeZone());
  }
  
}
