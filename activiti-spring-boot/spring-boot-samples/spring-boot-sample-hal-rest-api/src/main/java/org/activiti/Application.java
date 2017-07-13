package org.activiti;



import org.activiti.services.audit.producer.app.AuditProducerChannels;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableBinding(AuditProducerChannels.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


}
