/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.test.matchers;

public class IntermediateCatchEventMatchers extends ActivityMatchers {

    private static final String INTERMEDIATE_CATCH_EVENT = "intermediateCatchEvent";

    private IntermediateCatchEventMatchers(String definitionKey) {
        super(definitionKey);

    }

    @Override
    public String getActivityType() {
        return INTERMEDIATE_CATCH_EVENT;
    }

    public static IntermediateCatchEventMatchers intermediateCatchEvent(String definitionKey) {
        return new IntermediateCatchEventMatchers(definitionKey);
    }

}
