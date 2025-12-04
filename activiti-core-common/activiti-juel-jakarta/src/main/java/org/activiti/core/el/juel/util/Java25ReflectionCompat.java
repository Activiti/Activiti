/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
package org.activiti.core.el.juel.util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Java 25+ compatibility utility for secure reflection operations.
 * Provides fallback mechanisms for strict module access controls.
 */
public class Java25ReflectionCompat {

    private static final boolean IS_JAVA_25_PLUS;
    private static final MethodHandles.Lookup LOOKUP;

    static {
        int javaVersion = getJavaVersion();
        IS_JAVA_25_PLUS = javaVersion >= 25;
        LOOKUP = MethodHandles.lookup();
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    /**
     * Attempts to make a method accessible using Java 25+ compatible patterns.
     * Falls back to traditional setAccessible for older Java versions.
     *
     * @param method The method to make accessible
     * @return true if the method was successfully made accessible
     */
    public static boolean makeAccessible(Method method) {
        if (method == null) {
            return false;
        }

        if (!IS_JAVA_25_PLUS) {
            // Java < 25: use traditional approach
            try {
                method.setAccessible(true);
                return true;
            } catch (SecurityException e) {
                return false;
            }
        }

        // Java 25+: use MethodHandles.Lookup with privateLookupIn
        try {
            Class<?> declaringClass = method.getDeclaringClass();

            // Try to create private lookup for the declaring class
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(declaringClass, LOOKUP);

            // Unreflect the method through the private lookup
            privateLookup.unreflect(method);
            return true;

        } catch (IllegalAccessException e) {
            // If private lookup fails, try fallback strategies
            try {
                // Fallback 1: Try traditional setAccessible with security check
                method.setAccessible(true);
                return true;
            } catch (SecurityException se) {
                // Fallback 2: Check if method is already accessible
                return method.canAccess(null) || method.trySetAccessible();
            }
        } catch (Exception e) {
            // Last resort: try traditional approach
            try {
                method.setAccessible(true);
                return true;
            } catch (SecurityException se) {
                return false;
            }
        }
    }

    /**
     * Safely invokes a method with Java 25+ compatibility.
     * Uses MethodHandle.invoke when available for better performance and security.
     *
     * @param method The method to invoke
     * @param target The target object (null for static methods)
     * @param args The method arguments
     * @return The method result
     * @throws Exception if invocation fails
     */
    public static Object safeInvoke(Method method, Object target, Object... args) throws Exception {
        if (!IS_JAVA_25_PLUS) {
            // Java < 25: use traditional reflection
            return method.invoke(target, args);
        }

        try {
            // Java 25+: use MethodHandle for better security
            Class<?> declaringClass = method.getDeclaringClass();
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(declaringClass, LOOKUP);

            var methodHandle = privateLookup.unreflect(method);

            if (target == null) {
                // Static method
                return methodHandle.invokeWithArguments(args);
            } else {
                // Instance method
                Object[] allArgs = new Object[args.length + 1];
                allArgs[0] = target;
                System.arraycopy(args, 0, allArgs, 1, args.length);
                return methodHandle.invokeWithArguments(allArgs);
            }

        } catch (IllegalAccessException e) {
            // Fallback to traditional reflection
            if (!method.canAccess(target)) {
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (Throwable t) {
            // MethodHandle operations can throw Throwable
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw new Exception("MethodHandle invocation failed", t);
            }
        }
    }
}
