package org.activiti.engine.impl.util;

/**

 */
public class JvmUtil {

  public static String getJavaVersion() {
    return System.getProperty("java.version");
  }

  public static boolean isJDK8() {
    String version = System.getProperty("java.version");
    return version.startsWith("1.8");
  }

  public static boolean isJDK7() {
    String version = System.getProperty("java.version");
    return version.startsWith("1.7");
  }

  public static boolean isAtLeastJDK7() {
    return isJDK7() || isJDK8();
  }

}
