package org.activiti.spring.boot;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionServiceIT {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessExtensionService processExtensionService;


    @Test
    public void canReadExtension() throws IOException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("initialVarsProcess")
                .singleResult();

        boolean hasExtensionsFor = processExtensionService.hasExtensionsFor(processDefinition);

        assertThat(hasExtensionsFor).isTrue();

        ProcessExtensionModel model = processExtensionService.getExtensionsFor(processDefinition);

        assertThat(model).isNotNull();
        assertThat(model.getExtensions().getProperties()).containsKey("d440ff7b-0ac8-4a97-b163-51a6ec49faa1");
    }
}
