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
package org.activiti.dmn.engine.impl.mvel.extension;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author Yvo Swillens
 */
public class DateUtil {

    public static Date toDate(String dateString) {

        if (StringUtils.isEmpty(dateString)) {
            throw new IllegalArgumentException("date string cannot be empty");
        }

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate dateTime = dtf.parseLocalDate(dateString);

        return dateTime.toDate();
    }

    public static Date addDate(Date startDate, Integer years, Integer months, Integer days) {

        LocalDate currentDate = new LocalDate(startDate);

        currentDate = currentDate.plusYears(years);
        currentDate = currentDate.plusMonths(months);
        currentDate = currentDate.plusDays(days);

        return currentDate.toDate();
    }

    public static Date subtractDate(Date startDate, Integer years, Integer months, Integer days) {

        LocalDate currentDate = new LocalDate(startDate);

        currentDate = currentDate.minusYears(years);
        currentDate = currentDate.minusMonths(months);
        currentDate = currentDate.minusDays(days);

        return currentDate.toDate();
    }

    public static Date getCurrentDate() {

        return new LocalDate().toDate();
    }
}
