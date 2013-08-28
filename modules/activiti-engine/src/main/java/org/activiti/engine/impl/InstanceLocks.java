package org.activiti.engine.impl;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Saeid Mirzaei
 */

public class InstanceLocks {

  private static ConcurrentHashMap<String, String> instanceLocks = new ConcurrentHashMap<String, String>();

  public static  ConcurrentHashMap<String, String> getLocks() {
    return instanceLocks;
  }
}
