package org.activiti.rest.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Joram Barrez
 */
@Configuration
public class JacksonConfiguration {

    @Bean()
    public ObjectMapper objectMapper() {
        // To avoid instantiating and configuring the mapper everywhere
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

}
