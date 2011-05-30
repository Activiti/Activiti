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

package org.activiti.explorer.util.time;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.util.time.timeunit.DayTimeUnit;
import org.activiti.explorer.util.time.timeunit.HourTimeUnit;
import org.activiti.explorer.util.time.timeunit.MinuteTimeUnit;
import org.activiti.explorer.util.time.timeunit.MonthTimeUnit;
import org.activiti.explorer.util.time.timeunit.WeekTimeUnit;
import org.activiti.explorer.util.time.timeunit.YearTimeUnit;

/**
 * @author Frederik Heremans
 */
public class HumanTime {
  
  private static final List<TimeUnit> timeUnits = Arrays.asList(
          new YearTimeUnit(),
          new MonthTimeUnit(),
          new WeekTimeUnit(),
          new DayTimeUnit(),
          new HourTimeUnit(),
          new MinuteTimeUnit()
  );
  
  private Long baseDate;
  private I18nManager i18nManager;
  
  /**
   * Create human time, relative to current time.
   */
  public HumanTime(I18nManager i18nManager) {
    this(null, i18nManager);
  }

  /**
   * Create human time, relative to the given date.
   */
  public HumanTime(Date date, I18nManager i18nManager) {
    if(i18nManager == null) {
      throw new IllegalArgumentException("I18NManager is required!");
    }
    
    this.i18nManager = i18nManager;
    if(date != null) {
      baseDate = date.getTime();
    } else {
      baseDate = new Date().getTime();
    }
  }
  
  
  /**
   * Returns the human readable string of the duration between
   * the given date and the base date.
   */
  public String format(Date date) {
    boolean future = true;
    Long difference = date.getTime() - baseDate;
    if(difference < 0) {
      future = false;
      difference = -difference;
    } else if(difference == 0) {
      return i18nManager.getMessage(Messages.TIME_UNIT_JUST_NOW);
    }
    
    String unitMessage = getUnitMessage(difference);
    
    String messageKey = null;
    if(future) {
      messageKey = Messages.TIME_UNIT_FUTURE;
    } else {
      messageKey = Messages.TIME_UNIT_PAST;
    }
    
    return i18nManager.getMessage(messageKey, unitMessage); 
  }
  
  private String getUnitMessage(Long difference) {
    String unitMessage = null;
    TimeUnit unitToUse = null;
    TimeUnit currentUnit = null;
    
    for(int i=0; i<timeUnits.size() && unitToUse == null; i++) {
      currentUnit = timeUnits.get(i);
      
      if(currentUnit.getNumberOfMillis() <= difference) {
        unitToUse = currentUnit;
      }
    }
    
    if(unitToUse == null) {
      // No unit found, so use "moments ago" of "moments from now"
      unitMessage = i18nManager.getMessage(Messages.TIME_UNIT_MOMENTS);
    } else {
      // Calculate number of units
      Long numberOfUnits = (difference - (difference%unitToUse.getNumberOfMillis())) / unitToUse.getNumberOfMillis();
      unitMessage = i18nManager.getMessage(unitToUse.getMessageKey(numberOfUnits), numberOfUnits);
    }
    
    return unitMessage;
  }
  
}
