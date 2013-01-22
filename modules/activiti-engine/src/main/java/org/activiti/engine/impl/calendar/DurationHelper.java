/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.util.ClockUtil;
import org.joda.time.DateTime;

/**
 * helper class for parsing ISO8601 duration format (also recurring) and computing next timer date
 */
public class DurationHelper {

  Date start;

  Date end;

  Duration period;

  boolean isRepeat;

  int times;

  DatatypeFactory datatypeFactory;

  public DurationHelper(String expressionS) throws Exception {
    List<String> expression = Arrays.asList(expressionS.split("/"));
    datatypeFactory = DatatypeFactory.newInstance();

    if (expression.size() > 3 || expression.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Cannot parse duration");
    }
    if (expression.get(0).startsWith("R")) {
      isRepeat = true;
      times = expression.get(0).length() ==  1 ? Integer.MAX_VALUE : Integer.parseInt(expression.get(0).substring(1));
      expression = expression.subList(1, expression.size());
    }

    if (isDuration(expression.get(0))) {
      period = parsePeriod(expression.get(0));
      end = expression.size() == 1 ? null : parseDate(expression.get(1));
    } else {
      start = parseDate(expression.get(0));
      if (isDuration(expression.get(1))) {
        period = parsePeriod(expression.get(1));
      } else {
        end = parseDate(expression.get(1));
        period = datatypeFactory.newDuration(end.getTime()-start.getTime());
      }
    }
    if (start == null && end == null) {
      start = ClockUtil.getCurrentTime();
    }

  }

  public Date getDateAfter() {
    if (isRepeat) {
      return getDateAfterRepeat(ClockUtil.getCurrentTime());
    }
    //TODO: is this correct?
    if (end != null) {
      return end;
    }
    return add(start, period);
  }
  
  public int getTimes() {
    return times;
  }

  private Date getDateAfterRepeat(Date date) {
    if (start != null) {
      Date cur = start;
      for (int i=0;i<times && !cur.after(date);i++) {
        cur = add(cur, period);
      }
      return cur.before(date) ? null : cur;
    }
    Date cur = add(end, period.negate());;
    Date next = end;

    for (int i=0;i<times && cur.after(date);i++) {
      next = cur;
      cur = add(cur, period.negate());
    }
    return next.before(date) ? null : next;
  }

  private Date add(Date date, Duration duration) {
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    duration.addTo(calendar);
    return calendar.getTime();
  }

  private Date parseDate(String date) throws Exception {
      return DateTime.parse(date).toDate();
  }

  private Duration parsePeriod(String period) throws Exception {
      return datatypeFactory.newDuration(period);
  }

  private boolean isDuration(String time) {
    return time.startsWith("P");
  }

}
