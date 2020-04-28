package org.activiti.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionServiceIT {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessExtensionService processExtensionService;


    @Test
    public void canReadExtension() throws IOException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("Process_initialVarsProcess")
                .singleResult();

        boolean hasExtensionsFor = processExtensionService.hasExtensionsFor(processDefinition);

        assertThat(hasExtensionsFor).isTrue();

        Extension extensions = processExtensionService.getExtensionsFor(processDefinition);

        assertThat(extensions).isNotNull();
        assertThat(extensions.getProperties()).containsKey("d440ff7b-0ac8-4a97-b163-51a6ec49faa1");
    }
}
