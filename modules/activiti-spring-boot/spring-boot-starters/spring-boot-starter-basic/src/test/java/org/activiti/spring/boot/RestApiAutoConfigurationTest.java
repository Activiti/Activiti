package org.activiti.spring.boot;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRegistration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * unit tests for the REST API integration.
 *
 * @author Josh Long
 */
@Ignore
public class RestApiAutoConfigurationTest {

    private AnnotationConfigEmbeddedWebApplicationContext applicationContext;

    @Configuration
    @Import({EmbeddedServletContainerAutoConfiguration.class,
            DispatcherServletAutoConfiguration.class,
            ServerPropertiesAutoConfiguration.class,
            HttpMessageConvertersAutoConfiguration.class,
            WebMvcAutoConfiguration.class})
    public static class EmbeddedContainerConfiguration {
    }

    @Configuration
    @Import( RestApiAutoConfiguration.class)
    public static class RestApiConfiguration {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @After
    public void close() {
        this.applicationContext.close();
    }

    @Test
    public void testRestApiIntegration() throws Throwable {

        this.applicationContext = new AnnotationConfigEmbeddedWebApplicationContext();
        this.applicationContext.register(EmbeddedContainerConfiguration.class, RestApiConfiguration.class);
        this.applicationContext.refresh();

        for (ServletRegistration servletRegistration : this.applicationContext.getBeansOfType(ServletRegistration.class).values()) {
            System.out.println(servletRegistration);
        }
        String authenticationChallenge = "http://localhost:8080/api/process-definitions";

        RestTemplate restTemplate = this.applicationContext.getBean(RestTemplate.class);
        final AtomicBoolean received403 = new AtomicBoolean();
        received403.set(false);
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return true;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
                if (clientHttpResponse.getStatusCode().equals(HttpStatus.FORBIDDEN))
                    received403.set(true);
            }
        });
        restTemplate.getForEntity(authenticationChallenge, Map.class);

        org.junit.Assert.assertTrue(received403.get());


    }


}
