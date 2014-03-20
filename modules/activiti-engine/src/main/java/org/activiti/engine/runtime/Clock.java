package org.activiti.engine.runtime;

import java.util.Calendar;
import java.util.Date;

/**
 * This interface provides full access to the clock
 */
public interface Clock extends ClockReader {

  void setCurrentTime(Date currentTime);

  void setCurrentCalendar(Calendar currentTime);
  
  void reset();

}
