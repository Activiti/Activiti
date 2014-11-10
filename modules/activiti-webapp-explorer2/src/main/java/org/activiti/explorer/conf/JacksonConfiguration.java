package org.activiti.explorer.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Joram Barrez
 */
@Configuration
public class JacksonConfiguration {

    @Bean()
    public ObjectMapper objectMapper() {
        // To avoid instantiating and configuring the mapper everywhere
        ObjectMapper mapper = new ObjectMapper();
        return mapper;
    }

}
