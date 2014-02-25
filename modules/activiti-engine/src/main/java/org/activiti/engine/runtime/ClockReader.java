package org.activiti.engine.runtime;

import java.util.Date;

/**
 * This class provides clock reading functionality
 */
public interface ClockReader {

  Date getCurrentTime();

}
