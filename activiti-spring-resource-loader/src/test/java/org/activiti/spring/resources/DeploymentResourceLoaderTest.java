package org.activiti.spring.resources;

import org.activiti.engine.RepositoryService;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class DeploymentResourceLoaderTest {

    @Test
    public void rename_me() {


        RepositoryService service= Mockito.mock(RepositoryService.class);

        DeploymentResourceLoader deploymentResourceLoader = new DeploymentResourceLoader<>();



        deploymentResourceLoader.setRepositoryService(service);


        ResourceReader res = new ResourceReader() {
            @Override
            public Predicate<String> getResourceNameSelector() {
                return null;
            }

            @Override
            public Object read(InputStream inputStream) throws IOException {
                return null;
            }
        };
        deploymentResourceLoader.loadResourcesForDeployment("123456",res);
    }
}
