package org.activiti.test.spring.boot;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyApp {

  public static void main(String[] args) {
    SpringApplication.run(MyApp.class, args);
  }
  
  @Bean
  public MyConfigurer myConfigurer() {
    return new MyConfigurer();
  }

  public class MyConfigurer implements ProcessEngineConfigurationConfigurer {

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
      System.out.println("configure called");
    }

  }

}
