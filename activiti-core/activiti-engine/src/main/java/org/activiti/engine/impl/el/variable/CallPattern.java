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

public class CallPattern
{
    private String classNamePattern;
    private String methodPattern;
    private String methodParamsPattern;

    public CallPattern(String classNamePattern, String methodPattern, String methodParamsPattern) {
        this.classNamePattern = classNamePattern;
        this.methodPattern = methodPattern;
        this.methodParamsPattern = methodParamsPattern;
    }

    public String getClassNamePattern() {
        return classNamePattern;
    }

    public String getMethodPattern() {
        return methodPattern;
    }

    public String getMethodParamsPattern() {
        return methodParamsPattern;
    }

    public void setMethodParamsPattern(String methodParamsPattern) {
        this.methodParamsPattern = methodParamsPattern;
    }


    public static CallPattern parse(String callPattern) {
        String classRegExp = "";
        String methodNameRegExp = "";
        String methodParamsPattern = "";

        String classNamePattern = null;
        String methodPattern = null;

        if (callPattern.matches("(.+)\\.(.+)\\(.*\\)")) { // has method def with params
            // remove method def
            int indexOfMethodPatern = callPattern.lastIndexOf(".");
            methodPattern = callPattern.substring(indexOfMethodPatern + 1);
            classNamePattern = callPattern.substring(0, indexOfMethodPatern);
        } else {
            if (callPattern.matches("([a-zA-Z0-1_]+\\.)*([A-Z]+[a-zA-Z0-1_]*)\\.(.+)")) { // has method def without params
                int indexOfMethodPatern = callPattern.lastIndexOf(".");
                methodPattern = callPattern.substring(indexOfMethodPatern + 1);
                classNamePattern = callPattern.substring(0, indexOfMethodPatern);
            } else {
                // there are only packages def
                classNamePattern = callPattern;
            }
        }

        if (classNamePattern.contains("*")) {
            classRegExp = convertToRegExp(classNamePattern);
        } else {
            classRegExp = classNamePattern;
        }

        if (methodPattern != null) {
            String methodNamePattern = methodPattern.replaceAll("\\(.*\\)", ""); // remove the method params

            if (methodPattern.startsWith("(")) {
                methodParamsPattern = methodPattern.substring(methodNamePattern.length() + 1, methodPattern.length() - 2);
            }

            methodNameRegExp = convertToRegExp(methodNamePattern);
        }


        return new CallPattern(classRegExp, methodNameRegExp, methodParamsPattern);

    }


    private static String convertToRegExp(String pattern) {
        pattern = pattern
                .replaceAll("\\s|\\t", "") // remove spaces from the expressions
                .replaceFirst("^(\\*\\.)+", "*.")
                .replaceAll("(\\.\\*){2,}", ".*") // join multiple * matchers .*.*.* => .*
                .replaceAll("\\.", "\\\\.") // escape the package separator
                .replaceAll("\\*", ".*"); // * => .*

        if ("*".equalsIgnoreCase(pattern)) return null;

        return pattern;
    }
}
