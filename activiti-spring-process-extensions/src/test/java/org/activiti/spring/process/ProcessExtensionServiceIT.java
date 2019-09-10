package org.activiti.spring.process;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.process.conf.ProcessExtensionsAutoConfiguration;
import org.activiti.spring.process.conf.ProcessExtensionsConfiguratorAutoConfiguration;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionServiceIT {

    @Configuration
    @Import({ProcessExtensionsAutoConfiguration.class, ProcessExtensionsConfiguratorAutoConfiguration.class})
    @ComponentScan(basePackages = {"org.activiti.spring.process.model", "org.activiti.spring.process.variable"})
    static class ContextConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
        }

        @MockBean
        protected RepositoryService repositoryService;


    }

    @MockBean
    private RepositoryService repositoryService;

    @Autowired
    private ProcessExtensionService processExtensionService;

    @Test
    public void canReadExtension() throws IOException {
        ProcessExtensionModel model = processExtensionService.getExtensionsForId("initialVarsProcess");
//        assertThat(model.getId())
//                .contains("initialVarsProcess");

        assertThat(model).isNotNull();
        assertThat(model.getExtensions().getProperties()).containsKey("d440ff7b-0ac8-4a97-b163-51a6ec49faa1");
    }
}
