package org.activiti.services.history.app;


import org.activiti.services.history.app.events.ProcessEngineEventEntity;
import org.activiti.services.model.events.ProcessEngineEvent;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    EventStoreRestResource eventStoreRestResource;


    @StreamListener(HistoryConsumerChannels.HISTORY_CONSUMER)
    public synchronized void receive(ProcessEngineEventEntity event) {
        System.out.println(">>>> Recieved Event" + event);
        eventStoreRestResource.save(event);

    }


}
