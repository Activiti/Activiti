package org.activiti.api.runtime.test.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.runtime.model.impl.IntegrationContextImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class IntegrationContextImplTest {

    @Autowired
    private ObjectMapper objectMapper;

    @SpringBootApplication
    static class Application {

        @Bean
        public ObjectMapper objectMapper(Module customizeProcessModelObjectMapper) {
            return new ObjectMapper().registerModule(customizeProcessModelObjectMapper);
        }
    }

    @Test
    void testIntegrationContextInBoundVariables() throws JsonParseException, JsonMappingException, IOException {
        // given
        IntegrationContextImpl source = new IntegrationContextImpl();

        source.addInBoundVariable("amount",
                                  BigDecimal.valueOf(1000, 2));

        // when
        IntegrationContext target = exchangeIntegrationContext(source);

        // then
        assertThat(target.getInBoundVariables()).containsEntry("amount",
                                                               BigDecimal.valueOf(1000, 2));
    }


    @Test
    void testIntegrationContextOutBoundVariables() throws JsonParseException, JsonMappingException, IOException {
        // given
        IntegrationContextImpl source = new IntegrationContextImpl();

        source.addOutBoundVariable("amount",
                                   BigDecimal.valueOf(1000, 2));

        // when
        IntegrationContext result = exchangeIntegrationContext(source);

        // then
        assertThat(result.getOutBoundVariables()).containsEntry("amount",
                                                                BigDecimal.valueOf(1000, 2));
    }

    private IntegrationContext exchangeIntegrationContext(IntegrationContext source) throws IOException {
        return objectMapper.readValue(objectMapper.writeValueAsString(source),
                                      IntegrationContext.class);
    }

}
