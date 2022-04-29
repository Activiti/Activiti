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

import java.util.List;

public class ListResolverHelper {

    private static final String LIST_FUNCTION_NAME = "list";
    private static final String LIST_INVOKE_METHOD = "list";

    public static List<Object> list(Object... objects) {
        return List.of(objects);
    }

    private ListResolverHelper() {
    }

    public static void addListFunctions(ActivitiElContext elContext) throws NoSuchMethodException {
        elContext.setFunction("", LIST_FUNCTION_NAME, ListResolverHelper.class.getMethod(LIST_INVOKE_METHOD, Object[].class));
    }
}
