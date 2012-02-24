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

import java.util.Calendar;
import java.util.ResourceBundle;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.activiti.explorer.I18nManager;



/**
 * @author Frederik Heremans
 */
public class HumanTimeTest extends TestCase {

  public void testHumanTime() {
    // Set up the i18nManager
    I18nManager i18nManager = new DummyI18nManager();
    
    Calendar now = Calendar.getInstance();
    HumanTime humanTime = new HumanTime(now.getTime(), i18nManager);
    
    // Edge cases
    Assert.assertEquals("Just now", humanTime.format(now.getTime()));
    
    Calendar momentsAgo = (Calendar) now.clone();
    momentsAgo.add(Calendar.SECOND, -10);
    Assert.assertEquals("moments ago", humanTime.format(momentsAgo.getTime()));
    
    Calendar momentsFromNow = (Calendar) now.clone();
    momentsFromNow.add(Calendar.SECOND, 10);
    Assert.assertEquals("moments from now", humanTime.format(momentsFromNow.getTime()));
    
    
    // Minutes
    Calendar cal = (Calendar) now.clone();
    cal.add(Calendar.MINUTE, 1);
    Assert.assertEquals("one minute from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.SECOND, 190);
    Assert.assertEquals("3 minutes from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.MINUTE, -1);
    Assert.assertEquals("one minute ago", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.SECOND, -190);
    Assert.assertEquals("3 minutes ago", humanTime.format(cal.getTime()));
    
    // Hours
    cal = (Calendar) now.clone();
    cal.add(Calendar.HOUR, 1);
    Assert.assertEquals("one hour from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.MINUTE, 190);
    Assert.assertEquals("3 hours from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.HOUR, -1);
    Assert.assertEquals("one hour ago", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.MINUTE, -190);
    Assert.assertEquals("3 hours ago", humanTime.format(cal.getTime()));
    
    
    // Days
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, 1);
    Assert.assertEquals("one day from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.HOUR, 75);
    Assert.assertEquals("3 days from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, -1);
    Assert.assertEquals("one day ago", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.HOUR, -75);
    Assert.assertEquals("3 days ago", humanTime.format(cal.getTime()));
    
    
    // Weeks
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, 7);
    Assert.assertEquals("one week from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, 23);
    Assert.assertEquals("3 weeks from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, -7);
    Assert.assertEquals("one week ago", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, -23);
    Assert.assertEquals("3 weeks ago", humanTime.format(cal.getTime()));
    
    
    // Months
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, 31);
    Assert.assertEquals("one month from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, 95);
    Assert.assertEquals("3 months from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.MONTH, -1);
    Assert.assertEquals("one month ago", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, -65);
    Assert.assertEquals("2 months ago", humanTime.format(cal.getTime()));
    
    // Years
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, 365);
    Assert.assertEquals("one year from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, 365 * 2 + 20);
    Assert.assertEquals("2 years from now", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, -365);
    Assert.assertEquals("one year ago", humanTime.format(cal.getTime()));
    
    cal = (Calendar) now.clone();
    cal.add(Calendar.DAY_OF_YEAR, -365 * 2 - 20);
    Assert.assertEquals("2 years ago", humanTime.format(cal.getTime()));
    
  }
  
  private class DummyI18nManager extends I18nManager {
    
    private static final long serialVersionUID = 1L;

    public DummyI18nManager() {
      messages = ResourceBundle.getBundle("test-messages");
    }
  }
  
  
}
