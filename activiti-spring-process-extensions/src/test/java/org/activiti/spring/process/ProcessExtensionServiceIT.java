package org.activiti.spring.process;

import java.io.IOException;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionServiceIT {

    @MockBean
    private RepositoryService repositoryService;

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
