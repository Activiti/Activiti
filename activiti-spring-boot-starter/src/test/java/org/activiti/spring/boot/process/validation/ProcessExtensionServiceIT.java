package org.activiti.spring.boot.process.validation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.ProcessExtensionService;
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

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionServiceIT {


    @Autowired
    private ProcessExtensionService processExtensionService;

    @Test
    @Ignore
    public void canReadExtension() throws IOException {

        boolean hasExtensionsFor = processExtensionService.hasExtensionsFor("initialVarsProcess");

        System.out.println("hasExtensionsFor = " + hasExtensionsFor);
        ProcessExtensionModel model = processExtensionService.getExtensionsForId("initialVarsProcess");
//        assertThat(model.getId())
//                .contains("initialVarsProcess");

        assertThat(model).isNotNull();
        assertThat(model.getExtensions().getProperties()).containsKey("d440ff7b-0ac8-4a97-b163-51a6ec49faa1");
    }
}
