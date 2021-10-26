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
package org.activiti.spring.process.variable.types;

import java.util.Date;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.core.el.JuelExpressionResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateVariableTypeTest {

    private DateVariableType dateVariableType;
    private DateFormatterProvider provider = new DateFormatterProvider("yyyy-MM-dd[['T']HH:mm:ss[.SSS'Z']]");

    @BeforeEach
    public void setUp() {
        dateVariableType = new DateVariableType(Date.class, provider, new JuelExpressionResolver());
    }

    @Test
    public void test() {
        dateVariableType.parseFromValue("${now()}");
    }
}
