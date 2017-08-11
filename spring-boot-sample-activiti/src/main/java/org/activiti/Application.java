package org.activiti;



import org.activiti.configuration.ActivitiRuntimeBundle;
import org.activiti.services.events.ProcessEngineChannels;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ActivitiRuntimeBundle
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}