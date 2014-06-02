package org.activiti.crystalball.examples.tutorial.step01;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple counter implementation
 */
public class Counter {
  public static AtomicLong value = new AtomicLong(0);
}
