package org.activiti.spring.process;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.spring.process.autoconfigure.ProcessExtensionsAutoConfiguration;
import org.activiti.spring.process.autoconfigure.ProcessExtensionsConfiguratorAutoConfiguration;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    }

    @Autowired
    private ProcessExtensionService processExtensionService;

    @Test
    public void canReadExtension() throws IOException {
        Map<String, ProcessExtensionModel> models = processExtensionService.readProcessExtensions();
        assertThat(models).isNotEmpty();
        assertThat(models.values())
                .extracting(ProcessExtensionModel::getId)
                .contains("initialVarsProcess");

        ProcessExtensionModel initialVarsProcessModel = models.values().stream().filter(model -> model.getId().equals("initialVarsProcess")).findFirst().orElse(null);
        assertThat(initialVarsProcessModel).isNotNull();
        assertThat(initialVarsProcessModel.getExtensions().getProperties()).containsKey("d440ff7b-0ac8-4a97-b163-51a6ec49faa1");
    }
}