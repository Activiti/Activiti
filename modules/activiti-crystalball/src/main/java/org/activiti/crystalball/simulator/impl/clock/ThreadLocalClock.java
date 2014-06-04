package org.activiti.crystalball.simulator.impl.clock;

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


import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.Clock;
import org.springframework.beans.factory.FactoryBean;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class provides thread local clock implementation. Each thread can run its own simulation engine and
 * can behave according to its internal thread local clock
 */
public class ThreadLocalClock implements Clock {
  private volatile static ThreadLocal<Clock> THREAD_CLOCK = new ThreadLocal<Clock>();
  protected FactoryBean<Clock> clockFactory;

  public ThreadLocalClock(FactoryBean<Clock> clockFactory) {
    this.clockFactory = clockFactory;
  }
  @Override
  public void setCurrentTime(Date currentTime) {
    get().setCurrentTime(currentTime);
  }

  private Clock get() {
    if (THREAD_CLOCK.get() == null) {
      try {
        THREAD_CLOCK.set(clockFactory.getObject());
      } catch (Exception e) {
        throw new ActivitiException("Unable to get simulation clock", e);
      }
    }
    return THREAD_CLOCK.get();
  }

  @Override
  public void setCurrentCalendar(Calendar currentTime) {
    get().setCurrentCalendar(currentTime);
  }

  @Override
  public void reset() {
    get().reset();
  }

  @Override
  public Date getCurrentTime() {
    return get().getCurrentTime();
  }

  @Override
  public Calendar getCurrentCalendar() {
    return get().getCurrentCalendar();
  }

  @Override
  public Calendar getCurrentCalendar(TimeZone timeZone) {
    return get().getCurrentCalendar(timeZone);
  }

  @Override
  public TimeZone getCurrentTimeZone() {
    return get().getCurrentTimeZone();
  }
}
