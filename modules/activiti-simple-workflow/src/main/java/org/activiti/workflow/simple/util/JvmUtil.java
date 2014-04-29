package org.activiti.workflow.simple.util;

/**
 * @author jbarrez
 */
public class JvmUtil {

	public static String getJavaVersion() {
		return System.getProperty("java.version");
	}
	
	public static boolean isJDK8() {
		String version = System.getProperty("java.version");
		return version.startsWith("1.8");
	}
	
}
