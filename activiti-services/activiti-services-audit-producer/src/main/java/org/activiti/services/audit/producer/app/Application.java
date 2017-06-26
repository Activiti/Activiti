package org.activiti.services.audit.producer.app;


import org.springframework.messaging.support.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableBinding(AuditProducerChannels.class)
public class Application implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


    @Autowired
    private AuditProducerChannels producer;

    @Override
    public void run(String... strings) throws Exception {
        send("Hello World!");
    }

    public void send(String message) {
        System.out.println("Sending Message: " + message);
        producer.auditProducer().send(MessageBuilder.withPayload(message).build());
    }


}
