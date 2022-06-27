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
package org.activiti.spring.boot;

import org.activiti.runtime.api.impl.MappingAwareActivityBehaviorFactory;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class DefaultActivityBehaviorFactoryMappingConfigurerIT {

    @Autowired
    private SpringProcessEngineConfiguration processEngineConfiguration;

    @Test
    public void processEngineConfigurationShouldHaveSetMappingAwareActivityBehaviorFactoryAsActivityBehaviorFactory(){
        assertThat(processEngineConfiguration.getActivityBehaviorFactory())
                .isInstanceOf(MappingAwareActivityBehaviorFactory.class);
        assertThat(processEngineConfiguration.getBpmnParser().getActivityBehaviorFactory())
                .isInstanceOf(MappingAwareActivityBehaviorFactory.class);

    }


}
