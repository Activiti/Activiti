/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.el.variable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Vasile Dirla
 */
public class CallWhitelistUtil
{

    public static boolean isWhitelisted(Set<CallPattern> whitelistCallPatterns, Object base, Object method, Class<?>[] paramTypes, int paramCount) {
        if (base == null) {
            return true;
        }

        Class<?> clazz = null;
        // proxy objects need special handling
        if (AopUtils.isJdkDynamicProxy(base)) {
            clazz = AopUtils.getTargetClass(base);
        } else {
            clazz = base.getClass();
        }

        // check if the class is whitelisted (or maybe any superclass or interface is whitelisted in it's tree
        if (whitelistCallPatterns != null) {
            for (CallPattern nextCallPattern : whitelistCallPatterns) {
                if (nextCallPattern != null) {
                    boolean match = checkClassAgainstPattern(nextCallPattern, clazz, method, paramTypes, paramCount);
                    if (match) return true;
                }
            }
            /* for (String nextCallPattern : whitelistCallPatterns) {
                if (nextCallPattern != null) {
                    CallPattern callPattern = parseCallPattern(nextCallPattern);

                    boolean match = checkClassAgainstPattern(callPattern, clazz, method, paramTypes, paramCount);

                    if (match) return true;
                }
            }*/
        }

        return false;
    }

    private static boolean checkClassAgainstPattern(CallPattern callPattern, Class<?> clazz, Object method, Class<?>[] paramTypes, int paramCount) {
        String classRegExp = callPattern.getClassNamePattern();
        String methodNameRegExp = callPattern.getMethodPattern();
        String methodParamsPattern = callPattern.getMethodParamsPattern();


        if (callPattern.getClassNamePattern().contains("*")) {
            // check the class name by pattern

            boolean matching = false;

            List<Class<?>> classesToMatch = new ArrayList<>();
            classesToMatch.add(clazz);

            while (classesToMatch.size() > 0 && !matching) {
                clazz = classesToMatch.remove(0);

                String className = clazz.getCanonicalName();
                // check if the class name matches
                if (classRegExp == null || classRegExp.length() == 0 || className.matches(classRegExp)) {
                    matching = true;
                } else {

                    Class<?> superClass = clazz.getSuperclass();
                    if (superClass != null) {
                        classesToMatch.add(superClass);
                    }

                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces != null && interfaces.length > 0) {
                        Collections.addAll(classesToMatch, interfaces);
                    }
                }
            }

            if (matching) {
                matching = checkMethod(methodNameRegExp, methodParamsPattern, clazz, method, paramTypes, paramCount);
            }

            return matching;
        } else {
            // if does not contain an wildchar then it is a fully qualified class name so we can check if it's assignable
            Class<?> nextClass = null;
            try {
                nextClass = Class.forName(callPattern.getClassNamePattern());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            if ((nextClass != null) && nextClass.isAssignableFrom(clazz)) {
                // try to check if the method is accessible. based on the pattern.
                return checkMethod(methodNameRegExp, methodParamsPattern, clazz, method, paramTypes, paramCount);
            }
        }

        return false;
    }

    public static boolean checkMethod(String methodNameRegExp, String methodParamsPattern,
                                      Class<?> clazz, Object method, Class<?>[] paramTypes, int paramCount) {
        boolean matching = true;
        if (method != null &&
                (!StringUtils.isEmpty(methodNameRegExp) ||
                        !StringUtils.isEmpty(methodParamsPattern))) {

            String methodName = method.toString();

            Method declaredMethod = null;
            try {
                declaredMethod = clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            if (declaredMethod == null) {

                for (Method classMethod : clazz.getMethods()) {
                    if (classMethod.getName().equals(methodName)) {
                        int formalParamCount = classMethod.getParameterTypes().length;
                        if (classMethod.isVarArgs()) {
                            if (paramCount >= formalParamCount - 1) {
                                declaredMethod = classMethod;
                                break;
                            }
                        } else {
                            if (paramCount == formalParamCount) {
                                declaredMethod = classMethod;
                                break;
                            }
                        }

                    }
                }

            }

            if (declaredMethod != null) {
                if (!StringUtils.isEmpty(methodNameRegExp)) {
                    matching = declaredMethod.getName().matches(methodNameRegExp);
                }

                if (!StringUtils.isEmpty(methodParamsPattern)) {
                    matching = paramsMatch(methodParamsPattern, declaredMethod.getParameterTypes());
                }
            } else {
                matching = false;
            }

        }

        return matching;
    }


    private static boolean paramsMatch(String methodParamsRegExp, Class<?>[] parameterTypes) {
        // todo: will be nice to support also paramter types matching.
        return true;
    }

}
