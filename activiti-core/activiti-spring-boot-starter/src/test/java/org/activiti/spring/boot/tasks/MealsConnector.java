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

package org.activiti.spring.boot.tasks;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;

public class MealsConnector implements Connector {

    private AtomicInteger currentMealIndex = new AtomicInteger(0);

    private List<String> meals = Arrays.asList("pizza", "pasta");

    private List<String> sizes = Arrays.asList("small", "medium");

    @Override
    public IntegrationContext apply(
        IntegrationContext integrationContext) {
        int remainder = currentMealIndex.getAndIncrement() % meals.size();
        integrationContext.addOutBoundVariable("meal", meals.get(remainder));
        integrationContext.addOutBoundVariable("size", sizes.get(remainder));
        return integrationContext;
    }

}
