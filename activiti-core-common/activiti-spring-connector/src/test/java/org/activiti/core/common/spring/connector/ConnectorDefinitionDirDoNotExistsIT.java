package org.activiti.core.common.spring.connector;

import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.core.common.spring.connector.autoconfigure.ConnectorAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ConnectorAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application-not-exists-test.properties")
public class ConnectorDefinitionDirDoNotExistsIT {

    @Autowired
    private ConnectorDefinitionService connectorDefinitionService;

    @Test
    public void ConnectorDefinitionsDirDoNotExists() throws IOException {

        List<ConnectorDefinition> connectorDefinitions = connectorDefinitionService.get();

        assertThat(connectorDefinitions).isEmpty();
    }
}
