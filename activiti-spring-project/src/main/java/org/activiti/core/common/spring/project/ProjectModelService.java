package org.activiti.core.common.spring.project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.core.common.project.model.ProjectManifest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ProjectModelService {

    private String projectManifestFilePath;

    private final ObjectMapper objectMapper;

    private ResourcePatternResolver resourceLoader;

    public ProjectModelService(String path,
                               ObjectMapper objectMapper,
                               ResourcePatternResolver resourceLoader) {
        this.projectManifestFilePath = path;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    private Optional<Resource> retrieveResource() {

        Resource resource = resourceLoader.getResource(projectManifestFilePath);
        if (resource.exists()) {
            return Optional.of(resource);
        } else {
            return Optional.empty();
        }
    }

    private ProjectManifest read(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream,
                                      ProjectManifest.class);
    }

    public ProjectManifest loadProjectManifest() throws IOException {
        Optional<Resource> resourceOptional = retrieveResource();

        return read(resourceOptional
                            .orElseThrow(() -> new FileNotFoundException("'" + projectManifestFilePath + "' manifest not found."))
                            .getInputStream());
    }

    public boolean hasProjectManifest(){
        return retrieveResource().isPresent();
    }
}
