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

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class MapBusinessCalendarManager implements BusinessCalendarManager {
  
  private Map<String, BusinessCalendar> businessCalendars = new HashMap<String, BusinessCalendar>();

  public BusinessCalendar getBusinessCalendar(String businessCalendarRef) {
    return businessCalendars.get(businessCalendarRef);
  }
  
  public BusinessCalendarManager addBusinessCalendar(String businessCalendarRef, BusinessCalendar businessCalendar) {
    businessCalendars.put(businessCalendarRef, businessCalendar);
    return this;
  }
}
