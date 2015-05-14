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
import org.activiti.engine.runtime.ClockReader;


/**
 * @author Tom Baeyens
 */
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
      throw new ActivitiException("couldn't resolve duedate: "+e.getMessage(), e);
    }
  }

// Code below just left in for a while just in case it would be needed again.
//  
//  private static Map<String, Integer> units = new HashMap<String, Integer>();
//  static {
//    units.put("millis", Calendar.MILLISECOND);
//    units.put("seconds", Calendar.SECOND);
//    units.put("second", Calendar.SECOND);
//    units.put("minute", Calendar.MINUTE);
//    units.put("minutes", Calendar.MINUTE);
//    units.put("hour", Calendar.HOUR);
//    units.put("hours", Calendar.HOUR);
//    units.put("day", Calendar.DAY_OF_YEAR);
//    units.put("days", Calendar.DAY_OF_YEAR);
//    units.put("week", Calendar.WEEK_OF_YEAR);
//    units.put("weeks", Calendar.WEEK_OF_YEAR);
//    units.put("month", Calendar.MONTH);
//    units.put("months", Calendar.MONTH);
//    units.put("year", Calendar.YEAR);
//    units.put("years", Calendar.YEAR);
//  }
//  
//  public Date resolveDuedate(String duedate) {
//    Date resolvedDuedate = Clock.getCurrentTime();
//    
//    StringTokenizer tokenizer = new StringTokenizer(duedate, " and ");
//    while (tokenizer.hasMoreTokens()) {
//      String singleUnitQuantity = tokenizer.nextToken();
//      resolvedDuedate = addSingleUnitQuantity(resolvedDuedate, singleUnitQuantity);
//    }
//    
//    return resolvedDuedate;
//  }
//
//  protected Date addSingleUnitQuantity(Date startDate, String singleUnitQuantity) {
//    int spaceIndex = singleUnitQuantity.indexOf(' ');
//    if (spaceIndex==-1 || singleUnitQuantity.length() > spaceIndex+1) {
//      throw new ActivitiException("invalid duedate format: "+singleUnitQuantity);
//    }
//    
//    String quantityText = singleUnitQuantity.substring(0, spaceIndex);
//    Integer quantity = new Integer(quantityText);
//    
//    String unitText = singleUnitQuantity
//      .substring(spaceIndex+1)
//      .trim()
//      .toLowerCase();
//    
//    int unit = units.get(unitText);
//
//    GregorianCalendar calendar = new GregorianCalendar(); 
//    calendar.setTime(startDate);
//    calendar.add(unit, quantity);
//    
//    return calendar.getTime();
//  }
}
