/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.util;


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
