package activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.spring.integration.ActivitiInboundGateway;
import org.activiti.spring.integration.IntegrationActivityBehavior;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    IntegrationActivityBehavior activitiDelegate(ActivitiInboundGateway activitiInboundGateway) {
        return new IntegrationActivityBehavior(activitiInboundGateway);
    }

    @Bean
    ActivitiInboundGateway inboundGateway(ProcessEngine processEngine) {
        return new ActivitiInboundGateway(processEngine, "customerId", "projectId");
    }

    @Bean
    AnalysingService analysingService() {
        return new AnalysingService();
    }

    public static class AnalysingService {

        private final AtomicReference<String> stringAtomicReference
                = new AtomicReference<String>();

        public void dump(String projectId) {
            this.stringAtomicReference.set(projectId);
        }

        public AtomicReference<String> getStringAtomicReference() {
            return stringAtomicReference;
        }
    }

    @Bean
    IntegrationFlow inboundProcess(ActivitiInboundGateway inboundGateway) {
        return IntegrationFlows
                .from(inboundGateway)
                .handle(new GenericHandler<ActivityExecution>() {
                    @Override
                    public Object handle(ActivityExecution execution, Map<String, Object> headers) {
                        return MessageBuilder.withPayload(execution)
                                .setHeader("projectId", "3243549")
                                .copyHeaders(headers).build();
                    }
                })
                .get();
    }

    @Bean
    CommandLineRunner init(
            final AnalysingService analysingService,
            final RuntimeService runtimeService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {

                String integrationGatewayProcess = "integrationGatewayProcess";

                runtimeService.startProcessInstanceByKey(
                        integrationGatewayProcess, Collections.singletonMap("customerId", (Object) 232L));


                System.out.println("projectId=" + analysingService.getStringAtomicReference().get());

            }
        };
    } // ...


}

