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
package org.activiti.core.el;

import java.util.Date;
import java.util.TimeZone;

public class DateResolverHelper {

    private static final String NOW_FUNCTION_NAME = "now";
    private static final String NOW_INVOKE_METHOD = "now";

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

    public static final Date now() {
        return new Date();
    }

    private DateResolverHelper() {
    }

    public static void addDateFunctions(ActivitiElContext elContext) throws NoSuchMethodException {
        elContext.setFunction("", NOW_FUNCTION_NAME, DateResolverHelper.class.getMethod(NOW_INVOKE_METHOD));
    }
}
