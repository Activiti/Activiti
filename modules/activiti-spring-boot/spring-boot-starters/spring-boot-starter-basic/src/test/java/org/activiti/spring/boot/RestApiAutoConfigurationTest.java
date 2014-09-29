package org.activiti.spring.boot;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Josh Long
 */
public class RestApiAutoConfigurationTest {

    @Configuration
    @Import({EmbeddedServletContainerAutoConfiguration.class,
            DispatcherServletAutoConfiguration.class, MultipartAutoConfiguration.class,
            ServerPropertiesAutoConfiguration.class,
            DataSourceAutoConfiguration.class,
            DataSourceProcessEngineAutoConfiguration.DataSourceConfiguration.class,
            RestApiAutoConfiguration.class

    })
    protected static class BaseConfiguration {
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public ServerProperties serverProperties() {
            ServerProperties properties = new ServerProperties();
            properties.setPort(0);
            return properties;
        }

    }

    /*   @Configuration
       @Import({EmbeddedServletContainerAutoConfiguration.class,
               DispatcherServletAutoConfiguration.class,
               ServerPropertiesAutoConfiguration.class,
               HttpMessageConvertersAutoConfiguration.class,
               WebMvcAutoConfiguration.class,
               DataSourceAutoConfiguration.class,
               DataSourceProcessEngineAutoConfiguration.DataSourceConfiguration.class,
               RestApiAutoConfiguration.class
       })
       public static class RestApiConfiguration {

           @Bean
           public RestTemplate restTemplate() {
               return new RestTemplate();
           }
       }
   */
    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    private AnnotationConfigEmbeddedWebApplicationContext context;

    @Test
    public void testRestApiIntegration() throws Throwable {

        this.context = new AnnotationConfigEmbeddedWebApplicationContext(
                BaseConfiguration.class);

        RestTemplate restTemplate = this.context.getBean(RestTemplate.class) ;


        String authenticationChallenge = "http://localhost:" + this.context.getEmbeddedServletContainer().getPort() + "/api/process-definitions" ;


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
