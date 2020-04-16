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
