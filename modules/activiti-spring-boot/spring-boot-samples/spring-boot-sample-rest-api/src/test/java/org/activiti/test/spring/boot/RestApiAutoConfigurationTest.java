package org.activiti.test.spring.boot;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.activiti.spring.boot.RestApiAutoConfiguration;
import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * @author Josh Long
 * @author Vedran Pavic
 */
public class RestApiAutoConfigurationTest {

  @Configuration
  @Import({EmbeddedServletContainerAutoConfiguration.class,
          MultipartAutoConfiguration.class,
          ServerPropertiesAutoConfiguration.class,
          DataSourceAutoConfiguration.class,
          DataSourceProcessEngineAutoConfiguration.DataSourceProcessEngineConfiguration.class,
          SecurityAutoConfiguration.class,
          RestApiAutoConfiguration.class,
          JacksonAutoConfiguration.class
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

    this.context = new AnnotationConfigEmbeddedWebApplicationContext();
    this.context.register(BaseConfiguration.class);
    this.context.refresh();
    
    RestTemplate restTemplate = this.context.getBean(RestTemplate.class) ;

    String authenticationChallenge = "http://localhost:" + this.context.getEmbeddedServletContainer().getPort() + 
        "/repository/process-definitions" ;

    final AtomicBoolean received401 = new AtomicBoolean();
    received401.set(false);
    restTemplate.setErrorHandler(new ResponseErrorHandler() {
        @Override
        public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
          return true;
        }

        @Override
        public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
          if (clientHttpResponse.getStatusCode().equals(HttpStatus.UNAUTHORIZED))
            received401.set(true);
        }
    });
    
    ResponseEntity<String> response = restTemplate.getForEntity(authenticationChallenge, String.class);
    org.junit.Assert.assertTrue(received401.get());
  }
}
