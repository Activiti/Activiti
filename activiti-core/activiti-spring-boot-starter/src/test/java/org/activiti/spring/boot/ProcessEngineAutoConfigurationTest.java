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

import org.activiti.core.common.spring.project.ApplicationUpgradeContextService;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.process.validation.AsyncPropertyValidator;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.validator.ValidatorSet;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProcessEngineAutoConfigurationTest {

    @InjectMocks
    private ProcessEngineAutoConfiguration processEngineAutoConfiguration;

    @Mock
    private ApplicationUpgradeContextService applicationUpgradeContextServiceMock;

    @Test
    public void shouldAddAsyncPropertyValidatorWhenAsyncExecutorIsEnabled() {
        //given
        ActivitiProperties activitiProperties = new ActivitiProperties();
        activitiProperties.setAsyncExecutorActivate(false);
        SpringProcessEngineConfiguration conf = new SpringProcessEngineConfiguration(applicationUpgradeContextServiceMock);

        //when
        processEngineAutoConfiguration.addAsyncPropertyValidator(activitiProperties,
                                                                 conf);

        //then
        ProcessValidator processValidator = conf.getProcessValidator();
        assertThat(processValidator).isNotNull();
        assertThat(processValidator.getValidatorSets())
                .flatExtracting(ValidatorSet::getValidators)
                .haveExactly(1,
                             new Condition<>(validator -> validator instanceof AsyncPropertyValidator,
                                             "instance of AsyncPropertyValidator"));

    }

    @Test
    public void shouldNotAddAsyncPropertyValidatorWhenAsyncExecutorIsDisabled() {
        //given
        ActivitiProperties activitiProperties = new ActivitiProperties();
        activitiProperties.setAsyncExecutorActivate(true);
        SpringProcessEngineConfiguration conf = new SpringProcessEngineConfiguration(applicationUpgradeContextServiceMock);

        //when
        processEngineAutoConfiguration.addAsyncPropertyValidator(activitiProperties,
                                                                 conf);

        //then
        ProcessValidator processValidator = conf.getProcessValidator();
        assertThat(processValidator).isNull();
    }

}
