package org.activiti.core.common.spring.project.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.core.common.spring.project.ProjectModelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class ProjectModelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass(value = "org.springframework.http.converter.json.Jackson2ObjectMapperBuilder")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ProjectModelService projectModelService(@Value("${project.manifest.file.path:classpath:/default-app.json}") String absolutePath,
                                                   ObjectMapper objectMapper,
                                                   ResourcePatternResolver resourceLoader) {
        return new ProjectModelService(absolutePath,
                                       objectMapper,
                                       resourceLoader);
    }
}
