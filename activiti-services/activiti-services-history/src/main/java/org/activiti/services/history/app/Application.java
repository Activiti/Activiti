package org.activiti.services.history.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@SpringBootApplication
@EnableBinding(HistoryConsumerChannels.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


    @StreamListener(HistoryConsumerChannels.HISTORY_CONSUMER)
    public synchronized void receive(String message) {
        System.out.println("******************");
        System.out.println("At Hisotry");
        System.out.println("******************");
        System.out.println("Received message " + message);
    }


}
