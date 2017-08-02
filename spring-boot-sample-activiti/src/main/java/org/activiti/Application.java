package org.activiti;



import org.activiti.services.events.ProcessEngineChannels;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBinding(ProcessEngineChannels.class)
@ComponentScan("org.activiti")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}