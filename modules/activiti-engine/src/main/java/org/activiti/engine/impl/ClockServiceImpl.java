package org.activiti.engine.impl;

import org.activiti.engine.runtime.ClockReader;

import java.util.Date;

/**
 * this class add time extension to services
 */
public class ClockServiceImpl extends ServiceImpl {

  protected ClockReader clockReader;

  public ClockServiceImpl() {
    this.clockReader = null;
  }

  public ClockServiceImpl(ClockReader clockReader) {
    this.clockReader = clockReader;
  }

  public void setClockReader(ClockReader clockReader) {
    this.clockReader = clockReader;
  }

  protected Date getCurrentTime() {
    return clockReader.getCurrentTime();
  }
}
