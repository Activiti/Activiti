package org.activiti.rest.dmn.conf.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
